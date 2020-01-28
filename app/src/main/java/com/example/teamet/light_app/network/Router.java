package com.example.teamet.light_app.network;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Consumer;

import com.example.teamet.light_app.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Router extends Service {
    public static final int PORT = 4567;
    private final double POLLING_INTERVAL_SEC = 10.0;

    private P2pManager pm = null;
    private Server server;

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

        server = new Server(String.valueOf(PORT), Router.this, pm);
        server.execute();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    pm.requestIsGroupOwner(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isGroupOwner) {
                            if (!isGroupOwner) {
                                pm.requestIPAddr(new Consumer<InetAddress>() {
                                    @Override
                                    public void accept(InetAddress inetAddress) {
                                        try {
                                            Socket sc = new Socket(inetAddress, PORT);
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
                        Thread.sleep((long) (POLLING_INTERVAL_SEC * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }).start();

        return START_STICKY;
    }
}