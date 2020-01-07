package com.example.teamet.light_app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class Router extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification =
            (new NotificationCompat.Builder(this, "Light-App Router"))
                .setContentTitle("Light-App Router")
                .build();

        new Thread (
            new Runnable() {
                @Override
                public void run() {

                }
            }
        );

        startForeground(1, notification);

        return START_STICKY;
    }
}
