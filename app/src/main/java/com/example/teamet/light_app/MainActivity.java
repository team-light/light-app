package com.example.teamet.light_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//    ネットワーク設定
    public void netSet(View view){
        Log.v("Function", "netSet");

        Intent intent = new Intent(this, NetworkSetActivity.class);
        startActivity(intent);
    }

//    情報の表示
    public void pushTrainFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("type", "TRAIN");

        dispInfo(intent);
    }

    public void pushHazardFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("type", "HAZARD");

        dispInfo(intent);
    }

    public void pushMapFab(View view){
        Intent intent = new Intent(this, DisplayInfoActivity.class);
        intent.putExtra("type", "MAP");

        dispInfo(intent);
    }

    private void dispInfo(Intent intent){
        startActivity(intent);
    }

//    GISの表示
    public void viewGIS(View view){
        Log.v("Function", "viewGIS");

        Intent intent = new Intent(this, DisplayGISActivity.class);
        startActivity(intent);
    }

//    アプリの終了
    public void closeApp(View view){
        this.finish();
    }

//    fabの制御
    private enum ButtonState{
        OPEN,
        CLOESE
    }
    private ButtonState buttonState = ButtonState.CLOESE;

    public void infofab(View view){
        Log.v("button", "infofab");
        int iconWhile = (int) convertDp2Px(56, this.getApplicationContext());

        if (buttonState == ButtonState.CLOESE){
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
        int fabsid[] = {R.id.train, R.id.hazard, R.id.map};
        LinearLayout linearLayout;

        ObjectAnimator animator;
        for(int i=0; i<3; i++){
            linearLayout = findViewById(fabsid[i]);
            linearLayout.setVisibility(View.VISIBLE);
            animator = ObjectAnimator.ofFloat(linearLayout, "translationY", iconWhile*i + convertDp2Px(64, this.getApplicationContext()));
            animator.setDuration(200);
            animator.start();
        }

        linearLayout = findViewById(R.id.close);
        animator = ObjectAnimator.ofFloat(linearLayout, "translationY", convertDp2Px(168, this.getApplicationContext()));
        animator.setDuration(200);
        animator.start();

        buttonState = ButtonState.OPEN;
    }

    public void fabClose(){
        ObjectAnimator animator;
        LinearLayout trainfab = findViewById(R.id.train);
        animator = ObjectAnimator.ofFloat(trainfab, "translationY", 0);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator1){
                trainfab.setVisibility(View.GONE);
                super.onAnimationEnd(animator1);
            }
        });
        animator.start();

        LinearLayout hazardfab = findViewById(R.id.hazard);
        animator = ObjectAnimator.ofFloat(hazardfab, "translationY", 0);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator1){
                hazardfab.setVisibility(View.GONE);
                super.onAnimationEnd(animator1);
            }
        });
        animator.start();

        LinearLayout mapfab = findViewById(R.id.map);
        animator = ObjectAnimator.ofFloat(mapfab, "translationY", 0);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator1){
                mapfab.setVisibility(View.GONE);
                super.onAnimationEnd(animator1);
            }
        });
        animator.start();

        LinearLayout close = findViewById(R.id.close);
        animator = ObjectAnimator.ofFloat(close, "translationY", 0);
        animator.start();

        buttonState = ButtonState.CLOESE;
    }
}
