package com.example.teamet.light_app.network;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.util.Consumer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Consumer;

import com.example.teamet.light_app.R;
import com.example.teamet.light_app.database.DataBaseMake;
import com.example.teamet.light_app.source.JsonAsyncTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Router extends Service {
    public static final int PORT = 4567;
    private final double PERIOD_SEC = 10.0;

    private P2pManager pm = null;
    private Server server;
    private DataBaseMake dbm;


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
                    server = new Server(String.valueOf(PORT), Router.this, pm);
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
                                sendJsonToGroupOwner();
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
                                            sc.close();
                                            Log.v("Router", String.format("Sent empty data to group-owner [%s:%d].", inetAddress.toString(), PORT));
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
}
