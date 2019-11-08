package com.example.teamet.light_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//    ネットワーク設定
    public  void netSet(View view){
        Log.v("Function", "netSet");

        Intent intent = new Intent(this, NetworkSetActivity.class);
        startActivity(intent);
    }

//    情報の表示
    public void viewInfo(View view) {
        Log.v("Function", "viewInfo");

        Intent intent = new Intent(this, DisplayInfoActivity.class);
        startActivity(intent);
    }

//    GISの表示
    public void viewGIS(View view){
        Log.v("Function", "viewGIS");

        Intent intent = new Intent(this, DisplayGISActivity.class);
        startActivity(intent);
    }
}
