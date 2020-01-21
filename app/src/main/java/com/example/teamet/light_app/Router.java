package com.example.teamet.light_app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Consumer;

import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

public class Router extends Service {
    private InetAddress addr = null;
    private P2pManager pm = null;

    private final long WAIT_MILLI_SEC_REQUEST_IP_ADDR = 1000;

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

        return START_STICKY;
    }

    public InetAddress getIPAddr() throws Exception {
        if (addr == null) {
            requestIPAddr();
            Thread.sleep(WAIT_MILLI_SEC_REQUEST_IP_ADDR);
        }
        if (addr == null) {
            throw new Exception("ip address request timeout");
        }
        return addr;
    }

    public void requestIPAddr() {
        pm.requestIPAddr(new Consumer<InetAddress>() {
            @Override
            public void accept(InetAddress inetAddress) {
                Router.this.addr = inetAddress;
            }
        });
    }
}
