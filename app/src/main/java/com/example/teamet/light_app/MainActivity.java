package com.example.teamet.light_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    LinearLayout[] fabs;
    LinearLayout fab_alarm;
    LinearLayout fab_earthquake;
    LinearLayout fab_map;
    LinearLayout fab_net;
    LinearLayout fab_close;
    ObjectAnimator animator_fabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        fab_alarm = findViewById(R.id.alarm);
        fab_earthquake = findViewById(R.id.earthquake);
        fab_map = findViewById(R.id.map);

        fabs = new LinearLayout[3];
        fabs[0] = fab_alarm;
        fabs[1] = fab_earthquake;
        fabs[2] = fab_map;

        fab_net = findViewById(R.id.net);
        fab_close = findViewById(R.id.close);
    }

//    情報の表示
    public void pushAlarmFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("type", "ALERT");

        dispInfo(intent);
    }

    public void pushEarthquakeFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("type", "EARTHQUAKE");

        dispInfo(intent);
    }

    public void pushMapFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("type", "MAP");
        Toast.makeText(MainActivity.this, "未実装です", Toast.LENGTH_LONG).show();

        //dispInfo(intent);
    }

    private void dispInfo(Intent intent){
        startActivity(intent);
    }

    //    ネットワーク設定
    public void netSet(View view){
        Log.v("Function", "netSet");

        Intent intent = new Intent(this, NetworkSetActivity.class);
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
                fab_alarm.setVisibility(View.GONE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_earthquake, "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fab_earthquake.setVisibility(View.GONE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fab_map, "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fab_map.setVisibility(View.GONE);
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
