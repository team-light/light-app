package com.example.teamet.light_app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout[] fabs;
    private LinearLayout fab_alarm;
    private LinearLayout fab_earthquake;
    private LinearLayout fab_map;
    private LinearLayout fab_net;
    private LinearLayout fab_close;
    private ObjectAnimator animator_fabs;

    private static final int PERMISSION_WRITE_EX_STR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        fab_alarm = findViewById(R.id.main_menu_alarm);
        fab_earthquake = findViewById(R.id.main_menu_earthquake);
        fab_map = findViewById(R.id.main_menu_map);

        fabs = new LinearLayout[3];
        fabs[0] = fab_alarm;
        fabs[1] = fab_earthquake;
        fabs[2] = fab_map;

        fab_net = findViewById(R.id.main_menu_set);
        fab_close = findViewById(R.id.main_menu_close);

        /// パーミッション許可を取る
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.CHANGE_NETWORK_STATE,
                    }, PERMISSION_WRITE_EX_STR
                );
            }
        }
    }

    //    情報の表示
    public void pushAlarmFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("info_type", "alarm");

        dispInfo(intent);
    }
    public void pushEarthquakeFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("info_type", "earthquake");

        dispInfo(intent);
    }
    public void pushMapFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("info_type", "map");

        dispInfo(intent);
    }

    private void dispInfo(Intent intent){
        startActivity(intent);
    }

//    設定
    public void settingFab(View view){
        Log.v("Function", "setFab");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

//    アプリの終了
    public void closeApp(View view){
        this.finish();
    }

//    fabの制御
    private enum ButtonState{
        OPEN,
        CLOSE
    }
    private ButtonState buttonState = ButtonState.CLOSE;
    public void infoFab(View view){
        Log.v("button", "infoFab");
        int iconWhile = (int) convertDp2Px(56, this.getApplicationContext());

        if (buttonState == ButtonState.CLOSE){
            fabOpen(iconWhile);
        }else{
            fabClose();
        }
    }

    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    public void fabOpen(int iconWhile){
        for(int i=0; i<fabs.length; i++){
            fabs[i].setVisibility(View.VISIBLE);
            animator_fabs = ObjectAnimator.ofFloat(fabs[i], "translationY", iconWhile*i + convertDp2Px(64, this.getApplicationContext()));
            animator_fabs.setDuration(200);
            animator_fabs.start();
        }

        animator_fabs = ObjectAnimator.ofFloat(fab_net, "translationY", convertDp2Px(168, this.getApplicationContext()));
        animator_fabs.setDuration(200);
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_close, "translationY", convertDp2Px(168, this.getApplicationContext()));
        animator_fabs.setDuration(200);
        animator_fabs.start();

        buttonState = ButtonState.OPEN;
    }
    public void fabClose(){
        animator_fabs = ObjectAnimator.ofFloat(fab_alarm, "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fab_alarm.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_earthquake, "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fab_earthquake.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_map, "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fab_map.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_net, "translationY", 0);
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_close, "translationY", 0);
        animator_fabs.start();

        buttonState = ButtonState.CLOSE;
    }
}
