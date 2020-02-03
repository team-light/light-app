package com.example.teamet.light_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.teamet.light_app.database.DataBaseMake;
import com.example.teamet.light_app.source.JsonAsyncTask;

public class DisplayInfoActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private ConstraintLayout layout_alarm;
    private ConstraintLayout layout_earthquake;

    private TextView textView_alarm;
    private TextView textView_earthquake;
    private Spinner prefSpinner;
    private Spinner areaSpinner;

    private LinearLayout[] fabs;
    private LinearLayout fab_alarm;
    private LinearLayout fab_earthquake;
    private LinearLayout fab_map;

    ObjectAnimator animator_fabs;

    private DataBaseMake dbm;

    private enum DisplayState{
        ALARM, EARTHQUAKE, MAP
    }
    private enum ButtonState{
        OPEN,
        CLOSE
    }

    private DisplayState displayState;
    private ButtonState buttonState = ButtonState.CLOSE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);

        toolbar = findViewById(R.id.displayInfo_toolbar);

        dbm = new DataBaseMake(getApplicationContext());

        layout_alarm = findViewById(R.id.displayInfo_alarm);
        layout_earthquake = findViewById(R.id.displayInfo_earthquake);

        textView_alarm = findViewById(R.id.displayInfo_alarm_textView);
        textView_earthquake = findViewById(R.id.displayInfo_earthquake_textView);
        prefSpinner = findViewById(R.id.displayInfo_pref_spinner);
        areaSpinner = findViewById(R.id.displayInfo_area_spinner);

        fab_alarm = findViewById(R.id.displayInfo_menu_alarm);
        fab_earthquake = findViewById(R.id.displayInfo_menu_earthquake);
        fab_map = findViewById(R.id.displayInfo_menu_map);

        fabs = new LinearLayout[2];

        Intent intent = getIntent();
        String display =  intent.getStringExtra("info_type");
        switch (display){
            case "alarm":
                toolbar.setTitle(R.string.fab_text_alarm);
                layout_alarm.setVisibility(View.VISIBLE);
                displayState = DisplayState.ALARM;

                fabs[1] = fab_earthquake;
                fabs[0] = fab_map;

                break;

            case "earthquake":
                toolbar.setTitle(R.string.fab_text_earthquake);
                layout_earthquake.setVisibility(View.VISIBLE);
                displayState = DisplayState.EARTHQUAKE;
                readEqData();

                fabs[1] = fab_alarm;
                fabs[0] = fab_map;

                break;

            case "map":
                toolbar.setTitle(R.string.fab_text_map);
                fab_map.setVisibility(View.GONE);
                displayState = DisplayState.MAP;

                fabs[1] = fab_alarm;
                fabs[0] = fab_earthquake;

                break;
        }
        setSupportActionBar(toolbar);

        setPrefSpinner();
    }

    public void infoFab(View view){
        Log.v("button", "infoFab");
        int iconWhile = (int) convertDp2Px(64, this.getApplicationContext());

        if (buttonState == ButtonState.CLOSE){
            fabOpen(iconWhile);
        }else{
            fabClose();
        }
    }

    public void alarmFab(View view){
        toolbar.setTitle(R.string.fab_text_alarm);
        setSupportActionBar(toolbar);

        layout_alarm.setVisibility(View.VISIBLE);
        layout_earthquake.setVisibility(View.INVISIBLE);
        displayState = DisplayState.ALARM;

        infoFab(view);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                fabs[1] = fab_earthquake;
                fabs[0] = fab_map;
            }
        }, 300);
    }
    public void earthquakeFab(View view){
        toolbar.setTitle(R.string.fab_text_earthquake);
        setSupportActionBar(toolbar);

        layout_alarm.setVisibility(View.INVISIBLE);
        layout_earthquake.setVisibility(View.VISIBLE);
        displayState = DisplayState.EARTHQUAKE;
        readEqData();

        infoFab(view);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                fabs[1] = fab_alarm;
                fabs[0] = fab_map;
            }
        }, 300);
    }
    public void mapFab(View view){
        toolbar.setTitle(R.string.fab_text_map);
        setSupportActionBar(toolbar);

        layout_alarm.setVisibility(View.INVISIBLE);
        layout_earthquake.setVisibility(View.INVISIBLE);
        displayState = DisplayState.MAP;

        infoFab(view);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                fabs[1] = fab_alarm;
                fabs[0] = fab_earthquake;
            }
        }, 300);
    }

    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    public void fabOpen(int iconWhile){
        for(int i=0; i<fabs.length; i++){
            fabs[i].setVisibility(View.VISIBLE);
            animator_fabs = ObjectAnimator.ofFloat(fabs[i], "translationY", -1*iconWhile*i - convertDp2Px(64, this.getApplicationContext()));
            animator_fabs.setDuration(200);
            animator_fabs.start();
        }

        buttonState = ButtonState.OPEN;
    }
    public void fabClose(){
        animator_fabs = ObjectAnimator.ofFloat(fabs[0], "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fabs[0].setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        animator_fabs = ObjectAnimator.ofFloat(fabs[1], "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fabs[1].setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animator);
            }
        });
        animator_fabs.start();

        buttonState = ButtonState.CLOSE;
    }

    private void readEqData(){
        SQLiteDatabase db = dbm.getReadableDatabase();
        StringBuilder sbuilder = new StringBuilder();
        Cursor target = db.query(
                "eq_info",
                null,
                null,
                null,
                null,
                null,
                null
        );
        target.moveToFirst();
        for(int j=0;j<30;j++){
            Log.v("hoge",j+"");
            String datetime = target.getString(1);
            String hypocenter = target.getString(2);
            double north_lat = target.getDouble(3);
            double east_long = target.getDouble(4);
            int depth = target.getInt(5);
            double magnitude = target.getDouble(6);
            String max_int = target.getString(7);
            String city_list = target.getString(8);
            String message =target.getString(9);

            setFont(2,sbuilder,"最大震度 <big><big>"+max_int+"</big></big>　マグニチュード <big><big>"+magnitude+"</big></big>");
            if (depth==0) {
                setFont(4, sbuilder, "発生時刻 : " + datetime + "<br>震源地　 : " + hypocenter + "<br>深さ　　 : ごく浅い");
            }else{
                setFont(4, sbuilder, "発生時刻 : " + datetime + "<br>震源地　 : " + hypocenter + "<br>深さ　　 : 約"+depth/1000+"km");
            }
            setFont(6,sbuilder,city_list.replaceAll("\n", "<br>"));
            setFont(4,sbuilder,message);
            sbuilder.append("<br><br>");

            target.moveToNext();
        }
        target.close();
        textView_earthquake.setText(Html.fromHtml(sbuilder.toString()));
    }

    private void setFont(int h_size,StringBuilder sbuilder,String string){
        sbuilder.append("<h" + h_size + ">"+ string + "</h" + h_size + ">");
    }

    private void readData(String pref, String city) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        StringBuilder sbuilder = new StringBuilder();
        Cursor target = db.query(
                "alert_view",
                null,
                "pref_name=? AND city_name=?",
                new String[] {pref, city},
                null,
                null,
                null
        );
        target.moveToFirst();
        if(target.getCount() > 0) {
            String datetime = target.getString(0);
            String warn = target.getString(3);
            String message = target.getString(4);


            if (warn.equals("")) {
                sbuilder.append(pref).append(" ").append(city).append("\n")
                        .append(datetime).append(" 発表\n\n")
                        .append("気象警報・注意報は発表されていません");
            } else {
                sbuilder.append(pref).append(" ").append(city).append("\n")
                        .append(datetime).append(" 発表\n")
                        .append(warn).append("\n")
                        .append(message);
            }
        } else {
            sbuilder.append("");
        }
        target.close();
        textView_alarm.setText(sbuilder.toString());

    }

    private void setPrefSpinner() {
        SQLiteDatabase db = dbm.getReadableDatabase();
        Cursor pref = db.query(
                "pref",
                new String[] {"name"},
                null,
                null,
                null,
                null,
                "code ASC"
        );
        pref.moveToFirst();
        String[] list = new String[pref.getCount() + 1];
        list[0] = "都道府県";
        for(int i = 0; i < pref.getCount(); i++) {
            list[i + 1] = pref.getString(0);
            pref.moveToNext();
        }
        pref.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
        prefSpinner.setAdapter(adapter);
        prefSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String)parent.getSelectedItem();
                SQLiteDatabase db = dbm.getReadableDatabase();
                Cursor target = db.query(
                        "pref",
                        new String[] {"code"},
                        "name=?",
                        new String[] {selectedItem},
                        null,
                        null,
                        null
                );
                target.moveToFirst();
                if(target.getCount() > 0) setAreaSpinner(db, target.getInt(0));
                if(!selectedItem.equals("都道府県")) textView_alarm.setText(selectedItem);
                target.close();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                textView_alarm.setText("Hello World!");
            }
        });
    }

    private void setAreaSpinner(SQLiteDatabase db, int code) {
        Cursor area = db.query(
                "city",
                new String[] {"name"},
                (code * 100000) + "<=code AND code<" + ((code + 1) * 100000),
                null,
                null,
                null,
                "code ASC"
        );
        area.moveToFirst();
        String[] list = new String[area.getCount() + 1];
        list[0] = "市区町村";
        for(int i = 0; i < area.getCount(); i++) {
            list[i + 1] = area.getString(0);
            area.moveToNext();
        }
        area.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
        areaSpinner.setAdapter(adapter);
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                readData((String)prefSpinner.getSelectedItem(), (String)areaSpinner.getSelectedItem());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                textView_alarm.setText("Hello World!");
            }
        });
    }
}
