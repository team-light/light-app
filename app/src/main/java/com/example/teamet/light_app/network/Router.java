package com.example.teamet.light_app.network;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Consumer;
import android.util.Log;
import android.util.Pair;

import com.example.teamet.light_app.R;
import com.example.teamet.light_app.database.DataBaseMake;
import com.example.teamet.light_app.source.JsonAsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.lang.Double.NaN;

public class Router extends Service {
    public static final int PORT = 4567;
    private final double PERIOD_SEC = 30.0;
    public final int BUF_SIZE = 1024;

    private static final int TRY_CONNECT_TIMES = 10;
    private static final int CHECK_GROUP_TIMES = 5;

    public static final byte METHOD_GET = 0x00;
    public static final byte METHOD_POST = 0x01;
    public static final byte METHOD_POLL_LOAD = 0x02;

    public static final byte STATUS_FOUND = 0x00;
    public static final byte STATUS_NOT_FOUND = 0x01;

    private P2pManager pm = null;
    private Server server;
    private DataBaseMake dbm;
    private char[] buf = new char[BUF_SIZE];


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification =
            (new NotificationCompat.Builder(this, "com.light-app.test"))
                .setContentTitle("Light-App Router")
                .setContentText("Running Light-App Router")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(startId, notification);

        pm = new P2pManager(this);

        server = new Server(Router.this, pm);
        server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        new Thread(() -> {
            AtomicBoolean completed = new AtomicBoolean();

            while (true) {
                Log.v("Router", "Start period.");

                pm.discoverPeers(() -> {
                    refreshConnection(config -> {
                        server.logStatistics();

                        if (config == null) {
                            completed.set(true);
                            return;
                        }

                        loopAsync(TRY_CONNECT_TIMES, null, ConnectLoopArgs -> {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            pm.connect(config, is_succeed -> {
                                if (!is_succeed) {
                                    ConnectLoopArgs.second.accept(null);
                                    return;
                                }

                                loopAsync(CHECK_GROUP_TIMES, null, checkGroupLoopArgs -> {
                                    pm.requestIsGroupOwner(isGroupOwner -> {
                                        if (isGroupOwner == null) {
                                            Log.v("Router", "Group is not constructed.");
                                            try {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            checkGroupLoopArgs.second.accept(null);
                                        } else if (isGroupOwner) {
                                            Log.v("Router", "Group-owner is me.");
                                            pm.disconnectAll(() -> completed.set(true));
                                        } else {
                                            pm.requestIPAddr(inetAddress -> {
                                                if (inetAddress == null) {
                                                    Log.v("Router", "Failed fetching Group-owner IP address.");
                                                    pm.disconnectAll(() -> completed.set(true));
                                                    return;
                                                }

                                                try {
                                                    Socket sc = new Socket(inetAddress, PORT);
                                                    PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
                                                    BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

                                                    pw.write(METHOD_GET);
                                                    pw.flush();
                                                    Log.v("Router", String.format("Sent GET request to group-owner [%s:%d].", inetAddress.toString(), PORT));

                                                    String json = recv(br, buf);
                                                    saveJson(json, this);
                                                    Log.v("Router", String.format("Received json from group-owner [%s:%d] and saved.", inetAddress.toString(), PORT));

                                                    sc.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                } finally {
                                                    pm.disconnectAll(() -> completed.set(true));
                                                }
                                            });
                                        }
                                    });
                                }, state -> {
                                    completed.set(true);
                                });
                            });
                        }, state -> {
                            completed.set(true);
                        });
                    });
                });

                try {
                    Thread.sleep((long) (PERIOD_SEC * 1000));
                    while (!completed.get()) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    Log.v("Router", "Interrupted.");
                    e.printStackTrace();
                    return;
                }
            }
        }).start();

        dbm = new DataBaseMake(getApplicationContext());
        JsonAsyncTask asyncTask = new JsonAsyncTask(dbm.getReadableDatabase());
        asyncTask.execute();

        return START_STICKY;
    }

    public void onUpdatedJson() {
        pm.requestIsGroupOwner(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isGroupOwner) {
                if (isGroupOwner != null && !isGroupOwner) {
                    pm.requestIPAddr(new Consumer<InetAddress>() {
                        @Override
                        public void accept(InetAddress inetAddress) {
                            if (inetAddress == null) {
                                Log.v("Router", "Failed fetching Group-owner IP address in sendJsonToGroupOwner().");
                                return;
                            }

                            Log.v("Router", String.format("Sending json to group-owner [%s:%d] ...", inetAddress.toString(), PORT));

                            Client client = new Client(inetAddress, String.valueOf(PORT), Router.this);
                            client.execute();
                        }
                    });
                }
            }
        });
    }

    public static void saveJson(String data, Context context){
        try {
            Log.v("Router", "Saving JSON...");
            File file = new File(context.getFilesDir(), "data.json");
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file, "UTF-8");
            pw.print(data);
            pw.close();
            Log.v("Router", "Saved JSON.");
        } catch(IOException e) {
            Log.v("Router", "Failed saving JSON: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void sendJsonFile(Socket sc, boolean withHeader, Context context){
        Log.v("Router", "Sending JSON file...");

        try {
            File json = new File(context.getFilesDir(), "data.json");
            BufferedReader jsonBR = new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8"));
            String str = jsonBR.readLine();//送信するjsonファイルが1行のみである前提で1行しか読み込ませない

            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            if (withHeader) {
                pw.print(METHOD_POST);
            }
            pw.println(str);
            pw.flush();
        } catch(IOException e) {
            //toast = Toast.makeText(context, "ファイルの入出力中にエラーが発生しました"+e.toString(), duration);
            //toast.show();
            Log.v("Router", e.toString());
            e.printStackTrace();
        }
    }

    public static void sendData(Socket sc, String data, Byte method) {
        Log.v("Router", "Sending data...");

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            if (method != null) {
                pw.write(method);
            }
            pw.println(data);
            pw.flush();
        } catch(IOException e) {
            Log.v("Router", e.toString());
            e.printStackTrace();
        }
    }

    public static String recv(BufferedReader br, char[] buf) throws IOException {
        int lenRecved;
        StringBuilder sb = new StringBuilder();

        do {
            lenRecved = br.read(buf, 0, buf.length);
            if (lenRecved != -1) {
                sb.append(buf, 0, lenRecved);
            }
        } while (lenRecved == buf.length);

        return sb.toString();
    }


    public void refreshConnection(Consumer<WifiP2pConfig> onDeterminedConfig) {
        class LoopState {
            double minLoad;
            WifiP2pConfig minLoadConfig;
            WifiP2pConfig unknownLoadConfig;
        };

        pm.requestPeers(wifiP2pDevices -> {
            LoopState initial = new LoopState();
            initial.minLoad = NaN;
            initial.minLoadConfig = null;
            initial.unknownLoadConfig = null;

            foldAsync(wifiP2pDevices.iterator(), initial, arg -> {
                WifiP2pDevice dev = arg.first.first;
                LoopState state = arg.first.second;
                Consumer<LoopState> loopContinuation = arg.second;

                Log.v("Router", "device found: " + dev.deviceAddress);

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = dev.deviceAddress;
                config.groupOwnerIntent = 15; // 相手をgroup ownerにする
                Log.v("Router", "connecting...");

                pm.connect(config, is_succeed -> {
                    if (!is_succeed) {
                        pm.disconnectAll(() -> loopContinuation.accept(state));
                        return;
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pm.requestIsGroupOwner(isOwner -> {
                        if (isOwner == null) {
                            Log.v("Router", "isOwner == null");
                            pm.disconnectAll(() -> loopContinuation.accept(state));
                            return;
                        }
                        else if (isOwner) {
                            Log.v("Router", "owner is me");
                            state.unknownLoadConfig = config;
                            pm.disconnectAll(() -> loopContinuation.accept(state));
                            return;
                        }

                        pm.requestIPAddr(inetAddress -> {
                            if (inetAddress == null) {
                                pm.disconnectAll(() -> loopContinuation.accept(state));
                                return;
                            }

                            try {
                                Log.v("Router", "polling load...");
                                Socket sc = new Socket(inetAddress, PORT);
                                sendData(sc, "", METHOD_POLL_LOAD);
                                double load = recvLoad(sc);
                                if (state.minLoadConfig == null || load < state.minLoad) {
                                    state.minLoad = load;
                                    state.minLoadConfig = config;
                                }
                                Log.v("Router", "received load.");
                                Log.v("refreshConnection", dev.deviceAddress + ": " + load);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                pm.disconnectAll(() -> loopContinuation.accept(state));
                            }
                        });
                    });
                });
            }, state -> {
                if (state.minLoadConfig != null) {
                    onDeterminedConfig.accept(state.minLoadConfig);
                }
                else if (state.unknownLoadConfig != null) {
                    onDeterminedConfig.accept(state.unknownLoadConfig);
                }
                else {
                    onDeterminedConfig.accept(null);
                }
            });
        });
    }

    double recvLoad(Socket sc) throws IOException {
        String recved = recv(new BufferedReader(new InputStreamReader(sc.getInputStream())), buf);
        return Double.parseDouble(recved);
    }

    <T,State> void foldAsync(Iterator<T> iter, State initial, Consumer<Pair<Pair<T,State>,Consumer<State>>> f, Consumer<State> continuation) {
        if (!iter.hasNext()) {
            continuation.accept(initial);
            return;
        }

        T x = iter.next();
        f.accept(Pair.create(Pair.create(x, initial), s -> foldAsync(iter, s, f, continuation)));
    }

    <State> void loopAsync(int times, State initial, Consumer<Pair<Pair<Integer, State>,Consumer<State>>> f, Consumer<State> continuation) {
        foldAsync(new Iterator<Integer>(){
            int counter = 0;

            @Override
            public boolean hasNext() {
                return counter < times;
            }

            @Override
            public Integer next() {
                return counter += 1;
            }
        }, initial, args -> f.accept(args), continuation);
    }
}
