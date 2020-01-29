package com.example.teamet.light_app.source;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.teamet.light_app.DisplayInfoActivity;
import com.example.teamet.light_app.network.Router;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class JsonTask extends TimerTask {

    private final String TAG = "JsonTask";
    private final int READ_TIMEOUT = 30 * 1000;
    private final int CONNECT_TIMEOUT = 30 * 1000;

    private final String url = "http://ko-kon.sakura.ne.jp/light-app/json/data.json";
    private String file_path;

    private JSONArray timestamp;

    private SQLiteDatabase db;

    private Router router;


    public JsonTask(String file_path, SQLiteDatabase db, Router router) {
        super();
        this.file_path = file_path;
        this.timestamp = new JSONArray();
        this.db = db;
        this.router = router;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(this.url).openConnection();
            con.setReadTimeout(this.READ_TIMEOUT);
            con.setConnectTimeout(this.CONNECT_TIMEOUT);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(false);
            con.connect();

            if(con.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "connected");
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sbuilder = new StringBuilder();
                String str;
                while((str = br.readLine()) != null) {
                    sbuilder.append(str);
                }

                PrintWriter pr = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.file_path)))));
                pr.print(sbuilder);

                JSONObject json = new JSONObject(sbuilder.toString());
                if(json.getJSONArray("timestamp").equals(timestamp)) {
                    this.getInfo(json);
                }

                br.close();
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.file_path))));
                StringBuilder sbuilder = new StringBuilder();
                String str;
                while ((str = br.readLine()) != null) {
                    sbuilder.append(str);
                }

                JSONObject json = new JSONObject(sbuilder.toString());
                if(json.getJSONArray("timestamp").equals(this.timestamp)) {
                    this.getInfo(json);
                }

                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getInfo(JSONObject json){
        Log.d(TAG, "getInfo");
        try {
            // 気象警報・注意報
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
                this.db.update("warn_info", values, "city=" + code, null);
            }

            // 地震
            JSONArray earthquake = json.getJSONArray("earthquake");
            for(int i = 0; i < earthquake.length(); i++) {
                JSONObject target = (JSONObject)earthquake.get(i);
                if(!target.toString().equals("{}")) {
                    ContentValues values = new ContentValues();
                    values.put("time", target.getString("datetime"));
                    values.put("hypocenter", target.getString("hypocenter"));
                    values.put("north_lat", target.getDouble("north_lat"));
                    values.put("east_long", target.getDouble("east_long"));
                    values.put("depth", target.getInt("depth"));
                    values.put("magnitude", target.getDouble("magnitude"));
                    values.put("max_int", target.getString("max_int"));
                    values.put("city_list", target.getString("city_list"));
                    values.put("message", target.getString("message"));
                    this.db.update("eq_info", values, "code=" + i, null);
                }
            }

            Log.d(TAG, "end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
