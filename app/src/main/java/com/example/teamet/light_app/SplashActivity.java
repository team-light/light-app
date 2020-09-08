package com.example.teamet.light_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
//import android.support.v4.app.AppLaunchChecker;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamet.light_app.network.Router;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler hdl = new Handler();

        Intent serviceIntent = new Intent(getApplication(), Router.class);
        String channelID = "com.light-app.test";
        CharSequence channelName = "light-app channel";
        if(Build.VERSION.SDK_INT >= 26){
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        startService(serviceIntent);

        hdl.postDelayed(new splashHandler(), 500);
    }

    class splashHandler implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }
}