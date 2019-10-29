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

//    情報の表示
    public void viewInfo(View view) {
        Log.v("Function", "viewInfo");

        Intent intent = new Intent(this, DisplayInfoActivity.class);
        startActivity(intent);
    }
}
