package com.example.teamet.light_app;


import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class DataBaseMake extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "keihou.db";
    private static final String TABLE_NAME = "keihoudb";
    private static final String _ID = "_id";

    private Context context;

    DataBaseMake(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){

        db.execSQL("CREATE TABLE info(time TEXT, pref INTEGER, city INTEGER PRIMARY KEY, alert TEXT)");
        db.execSQL("CREATE TABLE pref(code INTEGER PRIMARY KEY, name TEXT, message TEXT)");
        db.execSQL("CREATE TABLE city(code INTEGER PRIMARY KEY, name TEXT)");
//        db.execSQL("CREATE TABLE alert(code INTEGER PRIMARY KEY, name TEXT)");
        Log.d("TAG", "onCreate");
        db.execSQL("CREATE VIEW alert_view AS SELECT time, pref.name, city.name, alert, message FROM pref, city, info WHERE info.pref = pref.code AND info.city = city.code");
        insert_info(db, "19/12/6 15:30",  1, 110000, "heavy snow");
        insert_info(db, "19/12/6 16:21", 1, 121300, "terrible typhoon");
        insert_info(db, "19/12/7 15:00", 13, 1310200, "LEVEL10 Earthquake");
        insert_info(db, "19/12/8 19:00", 40, 4056700, "matsubayasi");
        readAreaData(db);
        readPrefData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS info");
        db.execSQL("DROP TABLE IF EXISTS pref");
        db.execSQL("DROP TABLE IF EXISTS city");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){

        onUpgrade(db, oldVersion, newVersion);

    }

    public void insert_info(SQLiteDatabase db, String time, int pref, int city, String alert){
        ContentValues values = new ContentValues();
        values.put("time",time);
        values.put("pref",pref);
        values.put("city",city);
        values.put("alert", alert);
        db.insert("info", null, values);

    }

    public void readPrefData(SQLiteDatabase db){
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
                    values.put("message", "");
                    db.insert("pref", null, values);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
//            try{
//                am.close();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
        }
    }
    public void readAreaData(SQLiteDatabase db){
        Log.d("TAG", "readAreaData");
        AssetManager am = this.context.getAssets();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open("area_data.csv")));
            String line;
            while((line = br.readLine()) != null){
                String[] str = line.split(",");
                if(str.length >= 2) {
                    ContentValues values = new ContentValues();
                    values.put("code", Integer.parseInt(str[0]));
                    values.put("name", str[1]);
                    db.insert("city", null, values);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}