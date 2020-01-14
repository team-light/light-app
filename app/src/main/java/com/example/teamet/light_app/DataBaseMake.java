package com.example.teamet.light_app;


import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DataBaseMake extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "keihou.db";

    private Context context;

    DataBaseMake(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){

        db.execSQL("CREATE TABLE warn_info(time TEXT, pref INTEGER, city INTEGER PRIMARY KEY, alert TEXT, message TEXT)");
        db.execSQL("CREATE TABLE pref(code INTEGER PRIMARY KEY, name TEXT)");
        db.execSQL("CREATE TABLE city(code INTEGER PRIMARY KEY, name TEXT)");
        db.execSQL("CREATE TABLE eq_info(code INTEGER PRIMARY KEY, time TEXT, hypocenter TEXT, north_lat REAL, east_long REAL, depth INTEGER, magnitude REAL, max_int TEXT, city_list TEXT, message TEXT)");
//        db.execSQL("CREATE TABLE alert(code INTEGER PRIMARY KEY, name TEXT)");
        Log.d("TAG", "onCreate");
        db.execSQL("CREATE VIEW alert_view AS SELECT time, pref.name pref_name, city.name city_name, alert, message FROM pref, city, warn_info WHERE warn_info.pref = pref.code AND warn_info.city = city.code");
        db.execSQL("CREATE VIEW earthquake_view AS SELECT time, hypocenter, north_lat, east_long, depth, magnitude, max_int, city_list FROM pref, city, earthquake");

        readAreaData(db);
        readPrefData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS warn_info");
        db.execSQL("DROP TABLE IF EXISTS pref");
        db.execSQL("DROP TABLE IF EXISTS city");
        db.execSQL("DROP TABLE IF EXISTS earthquake");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){

        onUpgrade(db, oldVersion, newVersion);

    }

    private void readPrefData(SQLiteDatabase db){
        Log.d("TAG", "readPrefData");
        AssetManager am = this.context.getAssets();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open("pref_data.csv")));
            String line;
            while((line = br.readLine()) != null){
                String[] str = line.split(",");
                if(str.length >= 2) {
                    ContentValues values = new ContentValues();
                    values.put("code", Integer.parseInt(str[0]));
                    values.put("name", str[1]);
                    db.insert("pref", null, values);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void readAreaData(SQLiteDatabase db){
        Log.d("TAG", "readAreaData");
        AssetManager am = this.context.getAssets();
        String now = getDatetime();
        Log.d("TAG", now);
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open("area_data.csv")));
            String line;
            while((line = br.readLine()) != null){
                String[] str = line.split(",");
                if(str.length >= 2) {
                    ContentValues values = new ContentValues();
                    int code = Integer.parseInt(str[0]);
                    values.put("code", code);
                    values.put("name", str[1]);
                    db.insert("city", null, values);

                    Log.d("TAG", code + ":" + str[1]);

                    // init info
                    values.clear();
                    values.put("time", now);
                    values.put("pref", code / 100000);
                    values.put("city", code);
                    values.put("alert", "");
                    values.put("message", "");
                    db.insert("warn_info", null, values);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getDatetime() {
        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(Calendar.getInstance().getTime());
    }

}