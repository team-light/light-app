package com.example.teamet.light_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
//import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.example.teamet.light_app.database.DataBaseMake;
import com.example.teamet.light_app.map.MapManager;
import com.example.teamet.light_app.source.JsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class DisplayInfoActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton button_setmap;

    private ConstraintLayout layout_alarm;
    private ConstraintLayout layout_earthquake;
    private ConstraintLayout layout_map;

    private TextView textView_alarm;
    private TextView textView_earthquake;
    private Spinner prefSpinner;
    private Spinner areaSpinner;
    private  Spinner mapSpinner;
    private MapView mapView_map;

    private LinearLayout[] fabs;
    private LinearLayout fab_alarm;
    private LinearLayout fab_earthquake;
    private LinearLayout fab_map;
    private LinearLayout fab_set;

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

    private GraphicsOverlay mGraphicsOverlay;
    private Callout mCallout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);

        toolbar = findViewById(R.id.displayInfo_toolbar);

        dbm = new DataBaseMake(getApplicationContext());

        layout_alarm = findViewById(R.id.displayInfo_alarm);
        layout_earthquake = findViewById(R.id.displayInfo_earthquake);
        layout_map = findViewById(R.id.displayInfo_map);

        textView_alarm = findViewById(R.id.displayInfo_alarm_textView);
        textView_earthquake = findViewById(R.id.displayInfo_earthquake_textView);
        prefSpinner = findViewById(R.id.displayInfo_pref_spinner);
        areaSpinner = findViewById(R.id.displayInfo_area_spinner);
        mapSpinner = findViewById(R.id.displayInfo_map_spinner);
        mapView_map = findViewById(R.id.displayInfo_map_mapView);
        setupMap();

        fab_alarm = findViewById(R.id.displayInfo_menu_alarm);
        fab_earthquake = findViewById(R.id.displayInfo_menu_earthquake);
        fab_map = findViewById(R.id.displayInfo_menu_map);
        fab_set = findViewById(R.id.displayInfo_menu_set);

        fabs = new LinearLayout[3];

        Intent intent = getIntent();
        String display =  intent.getStringExtra("info_type");
        switch (display){
            case "alarm":
                toolbar.setTitle(R.string.fab_text_alarm);
                layout_alarm.setVisibility(View.VISIBLE);
                displayState = DisplayState.ALARM;
                mapView_map.pause();


                fabs[2] = fab_earthquake;
                fabs[1] = fab_map;
                fabs[0] = fab_set;

                break;

            case "earthquake":
                toolbar.setTitle(R.string.fab_text_earthquake);
                layout_earthquake.setVisibility(View.VISIBLE);
                displayState = DisplayState.EARTHQUAKE;
                mapView_map.pause();
                readEqData();


                fabs[2] = fab_alarm;
                fabs[1] = fab_map;
                fabs[0] = fab_set;

                break;

            case "map":
                toolbar.setTitle(R.string.fab_text_map);
                layout_map.setVisibility(View.VISIBLE);
                displayState = DisplayState.MAP;

                fabs[2] = fab_alarm;
                fabs[1] = fab_earthquake;
                fabs[0] = fab_set;

                break;
        }
        setSupportActionBar(toolbar);
        
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setPrefSpinner();
    }

    @Override
    protected void onPause() {
        if (mapView_map != null) {
            mapView_map.pause();
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView_map != null) {
            mapView_map.resume();
        }
    }
    @Override
    protected void onDestroy() {
        if (mapView_map != null) {
            mapView_map.dispose();
        }
        super.onDestroy();
    }

    public boolean onSupportNavigateUp(){
        finish();

        return super.onSupportNavigateUp();
    }

    public void infoFab(View view){
        Log.v("button", "infoFab");
        int iconWhile = (int) convertDp2Px(64, this.getApplicationContext());

        if (buttonState == ButtonState.CLOSE){
            fabOpen(iconWhile);
        }else{
            fabClose(view);
        }
    }
    public void alarmFab(View view){
        toolbar.setTitle(R.string.fab_text_alarm);
        setSupportActionBar(toolbar);

        layout_alarm.setVisibility(View.VISIBLE);
        layout_earthquake.setVisibility(View.INVISIBLE);
        layout_map.setVisibility(View.INVISIBLE);
        displayState = DisplayState.ALARM;
        mapView_map.pause();

        infoFab(view);

        new Handler().postDelayed(() -> {
            fabs[2] = fab_earthquake;
            fabs[1] = fab_map;
            fabs[0] = fab_set;
        }, 300);
    }
    public void earthquakeFab(View view){
        toolbar.setTitle(R.string.fab_text_earthquake);
        setSupportActionBar(toolbar);

        layout_alarm.setVisibility(View.INVISIBLE);
        layout_earthquake.setVisibility(View.VISIBLE);
        layout_map.setVisibility(View.INVISIBLE);
        displayState = DisplayState.EARTHQUAKE;
        mapView_map.pause();
        readEqData();

        infoFab(view);

        new Handler().postDelayed(() -> {
            fabs[2] = fab_alarm;
            fabs[1] = fab_map;
            fabs[0] = fab_set;
        }, 300);
    }
    public void mapFab(View view){
        toolbar.setTitle(R.string.fab_text_map);
        setSupportActionBar(toolbar);

        layout_alarm.setVisibility(View.INVISIBLE);
        layout_earthquake.setVisibility(View.INVISIBLE);
        layout_map.setVisibility(View.VISIBLE);
        displayState = DisplayState.MAP;
        mapView_map.resume();

        infoFab(view);

        new Handler().postDelayed(() -> {
            fabs[2] = fab_alarm;
            fabs[1] = fab_earthquake;
            fabs[0] = fab_set;
        }, 300);
    }
    public void setFab(View view){
        infoFab(view);
    }

    private static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    private void fabOpen(int iconWhile){
        for(int i=0; i<fabs.length; i++){
            fabs[i].setVisibility(View.VISIBLE);
            animator_fabs = ObjectAnimator.ofFloat(fabs[i], "translationY", -1*iconWhile*i - convertDp2Px(64, this.getApplicationContext()));
            animator_fabs.setDuration(200);
            animator_fabs.start();
        }

        buttonState = ButtonState.OPEN;
    }
    public void fabClose(View view){
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

        animator_fabs = ObjectAnimator.ofFloat(fabs[2], "translationY", 0);
        animator_fabs.setDuration(200);
        animator_fabs.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animator){
                fabs[2].setVisibility(View.INVISIBLE);
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
        for(int i=0;i<30 && i<target.getCount();i++){
            Log.v("hoge",i+"");
            String datetime = target.getString(1);
            String hypocenter = target.getString(2);
//            double north_lat = target.getDouble(3);
//            double east_long = target.getDouble(4);
            int depth = target.getInt(5);
            double magnitude = target.getDouble(6);
            String max_int = target.getString(7);

            String city_list = new String();
            try{
                JSONArray json_city_list = new JSONArray(target.getString(8));
                for(int j=json_city_list.length()-1; 0<j; j--){
                    JSONObject tag_ = json_city_list.getJSONObject(j);
                    int max_int_ = tag_.getInt("max_int");
                    city_list += "震度"+max_int_+": <br>\t";
                    if(0<max_int_){
                        JSONObject prefs = tag_.getJSONObject("prefs");
                        Iterator<String> pref_list = prefs.keys();
                        while (pref_list.hasNext()){
                            String pref = pref_list.next();
                            city_list += "\t["+pref+"]<br>\t";
                            JSONArray area = prefs.getJSONArray(pref);
                            for(int k=0; k<area.length(); k++) city_list += area.getString(k)+" ";
                            city_list += "<br><br>";
                        }
                    }
                }
            }catch (Exception e){
                city_list = "読み込みエラー<br>";
                e.printStackTrace();
            }

            String message =target.getString(9);

            setFont(2,sbuilder,"最大震度 <big><big>"+max_int+"</big></big>　マグニチュード <big><big>"+magnitude+"</big></big>");
            if (depth==0) {
                setFont(4, sbuilder, "発生時刻 : " + datetime + "<br>震源地　 : " + hypocenter + "<br>深さ　　 : ごく浅い");
            }else{
                setFont(4, sbuilder, "発生時刻 : " + datetime + "<br>震源地　 : " + hypocenter + "<br>深さ　　 : 約"+depth/1000+"km");
            }
            setFont(6,sbuilder,city_list);
            setFont(4,sbuilder,message);
            sbuilder.append("<br><br>");

            target.moveToNext();
        }
        target.close();
        textView_earthquake.setText(Html.fromHtml(sbuilder.toString()));
    }
    private void setFont(int h_size,StringBuilder sbuilder,String string){
        sbuilder.append("<h").append(h_size).append(">").append(string).append("</h").append(h_size).append(">");
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

    private void setupMap() {

        //テスト用データの入力
        try{
            SQLiteDatabase db = dbm.getWritableDatabase();
            //警報情報
            JSONObject json = new JSONObject("{\"warn\": {\"110000\": {\"datetime\": \"2020年08月31日 04:49\", \"warn\": \"濃霧注意報\", \"message\": \"石狩、空知、後志地方では、３１日昼前まで濃霧による視程障害に注意してください。\"}, \"110100\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110200\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110300\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110400\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110500\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110600\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110700\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110800\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"110900\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"111000\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"120200\": {\"datetime\": \"2020年08月31日 14:05\", \"warn\": \"大雨注意報 波浪注意報\", \"message\": \"渡島、檜山地方では、土砂災害に注意してください。渡島地方では、強風や高波に注意してください。\"}, \"120300\": {\"datetime\": \"2020年08月31日 04:49\", \"warn\": \"濃霧注意報\", \"message\": \"石狩、空知、後志地方では、３１日昼前まで濃霧による視程障害に注意してください。\"}, \"120400\": {\"datetime\": \"2020年08月31日 10:51\", \"warn\": \"\", \"message\": \"注意報を解除します。\"}, \"120500\": {\"datetime\": \"2020年08月31日 15:38\", \"warn\": \"波浪注意報\", \"message\": \"胆振、日高地方では、高波に注意してください。\"}, \"120600\": {\"datetime\": \"\", \"warn\": \"\", \"message\": \"\"}, \"120601\": {\"datetime\": \"2020年08月31日 16:34\", \"warn\": \"波浪注意報 濃霧注意報\", \"message\": \"根室、釧路地方では、１日明け方まで高波に、１日昼前まで濃霧による視程障害に注意してください。\"}, \"120602\": {\"datetime\": \"2020年08月31日 16:34\", \"warn\": \"濃霧注意報\", \"message\": \"根室、釧路地方では、１日明け方まで高波に、１日昼前まで濃霧による視程障害に注意してください。\"}, \"4720800\": {\"datetime\": \"2020年08月31日 13:29\", \"warn\": \"暴風警報 波浪警報 高潮警報 大雨注意報 雷注意報\", \"message\": \"沖縄本島地方では、３１日夜のはじめ頃から暴風に、３１日夕方から高波に警戒してください。本島中南部では、３１日夜のはじめ頃から１日昼前まで高潮に警戒してください。\"}, \"4720900\": {\"datetime\": \"2020年08月31日 13:29\", \"warn\": \"暴風警報 波浪警報 大雨注意報 雷注意報 高潮注意報\", \"message\": \"沖縄本島地方では、３１日夜のはじめ頃から暴風に、３１日夕方から高波に警戒してください。本島中南部では、３１日夜のはじめ頃から１日昼前まで高潮に警戒してください。\"}, \"4721000\": {\"datetime\": \"2020年08月31日 13:29\", \"warn\": \"暴風警報 波浪警報 高潮警報 大雨注意報 雷注意報\", \"message\": \"沖縄本島地方では、３１日夜のはじめ頃から暴風に、３１日夕方から高波に警戒してください。本島中南部では、３１日夜のはじめ頃から１日昼前まで高潮に警戒してください。\"}, \"4721100\": {\"datetime\": \"2020年08月31日 13:29\", \"warn\": \"暴風警報 波浪警報 高潮警報 大雨注意報 雷注意報\", \"message\": \"沖縄本島地方では、３１日夜のはじめ頃から暴風に、３１日夕方から高波に警戒してください。本島中南部では、３１日夜のはじめ頃から１日昼前まで高潮に警戒してください。\"}}}");
            JSONObject warn = json.getJSONObject("warn");
            JSONArray key = warn.names();
            for(int i = 0; i < key.length(); i++) {
                int code = Integer.parseInt(key.get(i).toString());
                JSONObject target = warn.getJSONObject(code + "");
                String datetime = target.getString("datetime");
                ContentValues values = new ContentValues();
                if(!datetime.equals("")) values.put("time", target.getString("datetime"));
                values.put("alert", target.getString("warn"));
                values.put("message", target.getString("message"));
                db.update("warn_info", values, "city=" + code, null);
            }

            //地震情報
            json = new JSONObject("{\"earthquake\": [{\"datetime\": \"2020年08月30日 14:37\", \"hypocenter\": \"茨城県南部\", \"north_lat\": 36.2, \"east_long\": 139.8, \"depth\": 60000, \"magnitude\": 4.4, \"max_int\": 3, \"city_list\": [{\"max_int\": 0, \"prefs\": {}}, {\"max_int\": 1, \"prefs\": {\"茨城県\": [\"高萩市\", \"那珂市\", \"大洗町\", \"河内町\", \"八千代町\"], \"栃木県\": [\"栃木さくら市\", \"益子町\", \"茂木町\", \"栃木那珂川町\", \"大田原市\", \"那須塩原市\", \"塩谷町\"], \"埼玉県\": [\"嵐山町\", \"吉見町\", \"鳩山町\", \"東秩父村\", \"埼玉神川町\", \"寄居町\", \"さいたま桜区\", \"さいたま浦和区\", \"さいたま南区\", \"さいたま緑区\", \"川越市\", \"所沢市\", \"飯能市\", \"草加市\", \"越谷市\", \"蕨市\", \"戸田市\", \"入間市\", \"朝霞市\", \"和光市\", \"新座市\", \"桶川市\", \"八潮市\", \"富士見市\", \"三郷市\", \"坂戸市\", \"鶴ヶ島市\", \"吉川市\", \"ふじみ野市\", \"伊奈町\", \"埼玉三芳町\", \"毛呂山町\", \"越生町\", \"川島町\", \"横瀬町\", \"皆野町\", \"小鹿野町\"], \"福島県\": [\"郡山市\", \"須賀川市\", \"田村市\", \"鏡石町\", \"泉崎村\", \"鮫川村\", \"石川町\", \"玉川村\", \"浅川町\", \"古殿町\", \"小野町\", \"いわき市\", \"福島広野町\", \"楢葉町\", \"川内村\", \"大熊町\", \"浪江町\", \"下郷町\", \"檜枝岐村\"], \"群馬県\": [\"中之条町\", \"東吾妻町\", \"川場村\", \"群馬昭和村\", \"みなかみ町\", \"高崎市\", \"藤岡市\", \"富岡市\", \"安中市\", \"みどり市\", \"榛東村\", \"吉岡町\", \"群馬上野村\", \"神流町\", \"甘楽町\", \"玉村町\"], \"千葉県\": [\"銚子市\", \"東金市\", \"旭市\", \"匝瑳市\", \"山武市\", \"多古町\", \"横芝光町\", \"一宮町\", \"長南町\", \"千葉中央区\", \"千葉若葉区\", \"千葉緑区\", \"千葉美浜区\", \"市川市\", \"船橋市\", \"千葉佐倉市\", \"市原市\", \"流山市\", \"我孫子市\", \"浦安市\", \"四街道市\", \"八街市\", \"富里市\", \"酒々井町\", \"君津市\"], \"東京都\": [\"東京中央区\", \"東京港区\", \"東京新宿区\", \"東京文京区\", \"東京台東区\", \"東京墨田区\", \"東京江東区\", \"東京目黒区\", \"東京大田区\", \"東京世田谷区\", \"東京渋谷区\", \"東京中野区\", \"東京杉並区\", \"東京豊島区\", \"東京北区\", \"東京荒川区\", \"東京板橋区\", \"東京練馬区\", \"東京葛飾区\", \"東京江戸川区\", \"八王子市\", \"武蔵野市\", \"三鷹市\", \"東京府中市\", \"町田市\", \"小金井市\", \"日野市\", \"東村山市\", \"国分寺市\", \"狛江市\", \"東大和市\", \"清瀬市\", \"武蔵村山市\", \"多摩市\", \"稲城市\", \"西東京市\", \"青梅市\"], \"神奈川県\": [\"横浜鶴見区\", \"横浜中区\", \"横浜保土ケ谷区\", \"横浜磯子区\", \"横浜港南区\", \"横浜瀬谷区\", \"横浜青葉区\", \"川崎川崎区\", \"川崎幸区\", \"川崎中原区\", \"川崎高津区\", \"平塚市\", \"茅ヶ崎市\", \"三浦市\", \"大和市\", \"座間市\", \"相模原緑区\", \"相模原中央区\", \"相模原南区\", \"秦野市\", \"厚木市\", \"中井町\", \"山北町\", \"湯河原町\", \"愛川町\", \"清川村\"], \"新潟県\": [\"南魚沼市\"], \"山梨県\": [\"甲府市\", \"山梨北杜市\", \"甲斐市\", \"甲州市\", \"富士川町\", \"富士吉田市\", \"大月市\", \"上野原市\", \"富士河口湖町\"], \"長野県\": [\"茅野市\", \"小海町\", \"長野南牧村\"], \"静岡県\": [\"東伊豆町\", \"富士宮市\", \"富士市\"]}}, {\"max_int\": 2, \"prefs\": {\"茨城県\": [\"日立市\", \"常陸太田市\", \"北茨城市\", \"笠間市\", \"ひたちなか市\", \"常陸大宮市\", \"小美玉市\", \"茨城町\", \"城里町\", \"東海村\", \"大子町\", \"茨城古河市\", \"石岡市\", \"結城市\", \"龍ケ崎市\", \"下妻市\", \"常総市\", \"取手市\", \"牛久市\", \"茨城鹿嶋市\", \"潮来市\", \"守谷市\", \"筑西市\", \"稲敷市\", \"かすみがうら市\", \"桜川市\", \"神栖市\", \"鉾田市\", \"つくばみらい市\", \"美浦村\", \"阿見町\", \"五霞町\", \"境町\", \"利根町\"], \"栃木県\": [\"足利市\", \"佐野市\", \"鹿沼市\", \"小山市\", \"真岡市\", \"那須烏山市\", \"上三川町\", \"市貝町\", \"芳賀町\", \"壬生町\", \"野木町\", \"日光市\", \"矢板市\"], \"埼玉県\": [\"熊谷市\", \"行田市\", \"加須市\", \"本庄市\", \"東松山市\", \"羽生市\", \"鴻巣市\", \"深谷市\", \"滑川町\", \"小川町\", \"ときがわ町\", \"埼玉美里町\", \"さいたま西区\", \"さいたま北区\", \"さいたま大宮区\", \"さいたま見沼区\", \"さいたま中央区\", \"さいたま岩槻区\", \"川口市\", \"春日部市\", \"狭山市\", \"上尾市\", \"北本市\", \"蓮田市\", \"幸手市\", \"白岡市\", \"杉戸町\", \"秩父市\", \"長瀞町\"], \"福島県\": [\"白河市\", \"棚倉町\", \"矢祭町\"], \"群馬県\": [\"沼田市\", \"片品村\", \"前橋市\", \"桐生市\", \"伊勢崎市\", \"太田市\", \"館林市\", \"渋川市\", \"板倉町\", \"群馬明和町\", \"千代田町\", \"大泉町\", \"邑楽町\"], \"千葉県\": [\"香取市\", \"神崎町\", \"芝山町\", \"千葉花見川区\", \"千葉稲毛区\", \"松戸市\", \"野田市\", \"成田市\", \"習志野市\", \"柏市\", \"八千代市\", \"鎌ケ谷市\", \"印西市\", \"白井市\", \"栄町\"], \"東京都\": [\"東京千代田区\", \"東京品川区\", \"東京足立区\", \"調布市\", \"小平市\"], \"神奈川県\": [\"横浜神奈川区\", \"横浜港北区\", \"横浜旭区\", \"横浜緑区\", \"川崎宮前区\"]}}, {\"max_int\": 3, \"prefs\": {\"茨城県\": [\"水戸市\", \"土浦市\", \"つくば市\", \"坂東市\", \"行方市\"], \"栃木県\": [\"宇都宮市\", \"栃木市\", \"下野市\", \"高根沢町\"], \"埼玉県\": [\"久喜市\", \"宮代町\"]}}], \"message\": \"この地震による津波の心配はありません。\"}]}");
            JSONArray earthquake = json.getJSONArray("earthquake");
            JSONObject target = (JSONObject)earthquake.get(0);
            ContentValues values = new ContentValues();
            values.put("time", target.getString("datetime"));
            values.put("hypocenter", target.getString("hypocenter"));
            values.put("north_lat", target.getDouble("north_lat"));
            values.put("east_long", target.getDouble("east_long"));
            values.put("depth", target.getInt("depth"));
            values.put("magnitude", target.getDouble("magnitude"));
            values.put("max_int", target.getString("max_int"));
            values.put("city_list", target.getJSONArray("city_list").toString());
            values.put("message", target.getString("message"));
            db.update("eq_info", values, "code=" + 0, null);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(mapSpinner != null){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, R.layout.support_simple_spinner_dropdown_item);
            mapSpinner.setAdapter(adapter);
            mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String tag = (String)mapSpinner.getSelectedItem();
                    switch (tag){
                        case "ピンなし":
                            setTouch();
                            break;
                        case "警報情報":
                            setTouchAlarm();
                            break;
                        case "地震情報":
                            setTouchEq();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        if (mapView_map != null) {
//            MapManager mapManager = new MapManager(this, mapView_map, dbm.getReadableDatabase());
//            mapManager.setMap();
            ArcGISRuntimeEnvironment.setLicense(getResources().getString(R.string.arcgis_license_key));
            Basemap.Type basemapType = Basemap.Type.STREETS_VECTOR;
            double[] tokyo = Prefectures.GetCoodinate(Prefectures.TOKYO);
            int levelOfDetail = 8;
            ArcGISMap map = new ArcGISMap(basemapType, tokyo[0], tokyo[1], levelOfDetail);
            mapView_map.setMap(map);

            createGraphics();
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setTouch(){
        mapView_map.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mapView_map) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point mapPoint = mapView_map.screenToLocation(screenPoint);
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                TextView calloutContent = new TextView(getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);

                int pref = Prefectures.GetNearPref(wgs84Point.getY(), wgs84Point.getX());
                calloutContent.setText(Prefectures.Pref[pref]);
                Point prefPoint = Prefectures.GetPoint(pref);

                mCallout = mapView_map.getCallout();
                mCallout.setLocation(prefPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                mapView_map.setViewpointCenterAsync(prefPoint);

                return true;
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchAlarm(){
        String[] alarmdata = new String[Prefectures.PREF_NUM];
        try{
            SQLiteDatabase db = dbm.getReadableDatabase();
            for(int i=0; i<alarmdata.length; i++){
                Cursor target = db.query(
                        "alert_view",
                        null,
                        "pref_name=?",
                        new String[] {Prefectures.Pref[i]},
                        null,
                        null,
                        null
                );
                target.moveToFirst();
                alarmdata[i] = "";
                Log.v("setTouchAlarm", Prefectures.Pref[i]+" "+target.getCount());
                for(int j=target.getCount(); 0<j; j--) {
                    String time = target.getString(0);
                    String area = target.getString(2);
                    String alarm = target.getString(3);
                    if(alarm.contains("警報")){
                        alarmdata[i] += time+" "+area+" "+alarm+"\n";
                        Log.v("setTouchAlarm", ">>"+ time+" "+area+" "+alarm);
                    }
                    target.moveToNext();
                }
                target.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mapView_map.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mapView_map) {

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point mapPoint = mapView_map.screenToLocation(screenPoint);
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                TextView calloutContent = new TextView(getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);

                int pref = Prefectures.GetNearPref(wgs84Point.getY(), wgs84Point.getX());
                calloutContent.setText(Prefectures.Pref[pref]+"\n"+alarmdata[pref]);
                Point prefPoint = Prefectures.GetPoint(pref);

                mCallout = mapView_map.getCallout();
                mCallout.setLocation(prefPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                mapView_map.setViewpointCenterAsync(prefPoint);

                return true;
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchEq(){
        String[] eqdata = new String[Prefectures.PREF_NUM];
        for(int i=0; i<eqdata.length; i++) eqdata[i] = "";
        try{
            SQLiteDatabase db = dbm.getReadableDatabase();
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
            JSONArray city_list = new JSONArray(target.getString(8));
            target.close();
            for(int i=city_list.length()-1; 0<i; i--){
                JSONObject tag_ = city_list.getJSONObject(i);
                int max_int = tag_.getInt("max_int");
                if(0<max_int){
                    JSONObject prefs = tag_.getJSONObject("prefs");
                    Iterator<String> pref_list = prefs.keys();
                    while (pref_list.hasNext()){
                        String pref = pref_list.next();
                        int pref_index = Prefectures.GetIndex(pref);
                        eqdata[pref_index] += "震度："+max_int+"\n";
                        JSONArray area = prefs.getJSONArray(pref);
                        for(int j=0; j<area.length(); j++) eqdata[pref_index] += area.getString(j)+", ";
                        eqdata[pref_index] += "\n";
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mapView_map.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mapView_map) {

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point mapPoint = mapView_map.screenToLocation(screenPoint);
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                TextView calloutContent = new TextView(getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);

                int pref = Prefectures.GetNearPref(wgs84Point.getY(), wgs84Point.getX());
                calloutContent.setText(Prefectures.Pref[pref]+"\n"+eqdata[pref]);
                Point prefPoint = Prefectures.GetPoint(pref);

                mCallout = mapView_map.getCallout();
                mCallout.setLocation(prefPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                mapView_map.setViewpointCenterAsync(prefPoint);

                return true;
            }
        });
    }
    private void createGraphics() {
        createGraphicsOverlay();
        createPointGraphics();
    }
    private void createGraphicsOverlay() {
        mGraphicsOverlay = new GraphicsOverlay();
        mapView_map.getGraphicsOverlays().add(mGraphicsOverlay);
    }
    private void createPointGraphics() {
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f));
        for(int i=0; i< Prefectures.PREF_NUM; i++){
            Graphic pointGraphic = new Graphic(Prefectures.GetPoint(i), pointSymbol);
            mGraphicsOverlay.getGraphics().add(pointGraphic);
        }
    }
}
