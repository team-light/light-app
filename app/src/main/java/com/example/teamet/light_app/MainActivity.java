package com.example.teamet.light_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    private Client cl;
    private EditText et;
    private EditText ip;
    private EditText port;
    private Button send;
    private Button cn;
    private Button trans;

    public boolean isEmpty(EditText editText){
        return editText.getText().toString().trim().length() == 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = this.findViewById(R.id.editText);
        ip = this.findViewById(R.id.IP);
        port = this.findViewById(R.id.PORT);
        send = this.findViewById(R.id.send);
        cn = this.findViewById(R.id.connect);
        trans = this.findViewById(R.id.transition);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cx = "";
                if(!isEmpty(ip) && !isEmpty(port) && !isEmpty(et)) {
                    cl = new Client(et, ip, port, getApplicationContext());
                    cl.execute(cx);
                }else{}
            }
        });

        trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubActivity.class));
            }
        });

        Intent serviceIntent = new Intent(getApplication(), Router.class);
        String channelID = "com.light-app.test";
        CharSequence channelName = "light-app channel";
        if(Build.VERSION.SDK_INT >= 26){
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        startService(serviceIntent);
    }
}