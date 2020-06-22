package com.example.teamet.light_app.network;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Consumer;
import android.util.Log;

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
import java.net.InetAddress;
import java.net.Socket;

public class Router extends Service {
    public static final int PORT = 4567;
    private final double PERIOD_SEC = 10.0;
    public final int BUF_SIZE = 1024;

    public static final byte METHOD_GET = 0x00;
    public static final byte METHOD_POST = 0x01;

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

        pm.requestIsGroupOwner(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isGroupOwner) {
                if (isGroupOwner) {
                    server = new Server(Router.this, pm);
                    server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.v("Router", "Start period.");

                    pm.requestIsGroupOwner(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isGroupOwner) {
                            if (isGroupOwner) {
                                Log.v("Router", "Group-owner is me.");
                            }
                            else {
                                pm.requestIPAddr(new Consumer<InetAddress>() {
                                    @Override
                                    public void accept(InetAddress inetAddress) {
                                        if (inetAddress == null) {
                                            Log.v("Router", "Failed fetching Group-owner IP address.");
                                            return;
                                        }

                                        try {
                                            Socket sc = new Socket(inetAddress, PORT);
                                            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
                                            BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

                                            pw.print(METHOD_GET);
                                            pw.flush();
                                            Log.v("Router", String.format("Sent GET request to group-owner [%s:%d].", inetAddress.toString(), PORT));

                                            String json = recv(br, buf);
                                            saveJson(json);
                                            Log.v("Router", String.format("Received json from group-owner [%s:%d] and saved.", inetAddress.toString(), PORT));

                                            sc.close();

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });

                    try {
                        Thread.sleep((long) (PERIOD_SEC * 1000));
                    } catch (InterruptedException e) {
                        Log.v("Router", "Interrupted.");
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }).start();

        dbm = new DataBaseMake(getApplicationContext());
        JsonAsyncTask asyncTask = new JsonAsyncTask(dbm.getReadableDatabase(), this);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return START_STICKY;
    }

    public void onUpdatedJson() {
        pm.requestIsGroupOwner(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isGroupOwner) {
                if (!isGroupOwner) {
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

    public static void saveJson(String data){
        try {
            Log.v("Router", "Saving JSON...");
            PrintWriter pw = new PrintWriter("assets\\data.json", "UTF-8");
            pw.print(data);
            pw.close();
            Log.v("Router", "Saved JSON.");
        } catch(IOException e) {
            Log.v("Router", "Failed saving JSON: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void sendJsonFile(Socket sc, boolean withHeader){
        Log.v("Router", "Sending JSON file...");

        try {
            File json = new File("assets\\data.json");
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

}
