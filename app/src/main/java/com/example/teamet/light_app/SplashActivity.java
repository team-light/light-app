package com.example.teamet.light_app;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        ネットワークの設定

        if(!AppLaunchChecker.hasStartedFromLauncher(this)){
            /*
                アプリ初起動時の通信（データベースの受け取り）

                インターネット(公衆Wi－Fi)につながっているデバイス:
                    インターネット上からjsonファイルをとってくる

                インターネットに未接続のデバイス:
                    ネットワーク上のデバイスからjsonファイルをもらう
            */
        }

        Handler hdl = new Handler();
        hdl.postDelayed(new splashHandler(), 1000);
    }

    class splashHandler implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }
}