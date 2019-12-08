package com.example.teamet.light_app;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        try {
            copy2Local();
        } catch (IOException e) {

        }

        Handler hdl = new Handler();
        hdl.postDelayed(new splashHandler(), 2000);
    }

    class splashHandler implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }

    private void copy2Local() throws IOException {
        // assetsから読み込み、出力する
        AssetManager assetManager = getResources().getAssets();
        String[] fileList = null;

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        fileList = assetManager.list("Aurora_CO_shp");
        for (String file : fileList) {
            Log.v("SplashActivity", file);

            inputStream = assetManager.open("Aurora_CO_shp/"+file);
            Log.v("SplashActivity", "input_opened");

            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
            Log.v("SplashActivity", "output_opened");

            int DEFAULT_BUFFER_SIZE = 1024*4;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            int n = 0;
            while( (n = inputStream.read(buffer)) != -1){
                fileOutputStream.write(buffer, 0, n);
            }
            Log.v("SplashActivity", "read_and_write_finished");
        }

        fileOutputStream.close();
        inputStream.close();
        Log.v("SplashActivity", "file_closed");
    }

}