package com.example.teamet.light_app;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

public class JsonTask extends TimerTask {

    private final String TAG = "JsonTask";
    private final int READ_TIMEOUT = 30 * 1000;
    private final int CONNECT_TIMEOUT = 30 * 1000;

    private final String url = "http://ko-kon.sakura.ne.jp/light-app/json/data.json";

    private MainActivity ma;
    private SQLiteDatabase db;
    private TextView textView;
    private Spinner prefSpinner;
    private Spinner areaSpinner;


//    public JsonTask(SQLiteDatabase db) {
//        super();
//        this.db = db;
//    }

    public JsonTask(MainActivity ma, SQLiteDatabase db) {
        super();
        this.ma = ma;
        this.db = db;
    }

    @Override
    public void run() {
        this.getInfo();
    }

    private void getInfo(){
        Log.d(TAG, "getInfo");

        try {
            HttpURLConnection con = (HttpURLConnection)new URL(this.url).openConnection();
            con.setReadTimeout(this.READ_TIMEOUT);
            con.setConnectTimeout(this.CONNECT_TIMEOUT);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(false);
            con.connect();

            Log.d(TAG, "connected");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder builder = new StringBuilder();
            String str;
            while((str = br.readLine()) != null) {
                builder.append(str);
            }


            JSONObject json = new JSONObject(builder.toString());

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
                    values.put("city_list", target.getJSONObject("city_list").toString());
                    values.put("message", target.getString("message"));
                    this.db.update("eq_info", values, "code=" + i, null);
                }
            }

            Log.d(TAG, "end");

            ma.reload();

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
