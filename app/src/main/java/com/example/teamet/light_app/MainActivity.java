package com.example.teamet.light_app;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Spinner prefSpinner;
    private Spinner areaSpinner;
    private DataBaseMake dbm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DB作成
        dbm = new DataBaseMake(getApplicationContext());


        // 変数textViewに表示するテキストビューのidを格納
        textView = findViewById(R.id.text_view);
        prefSpinner = findViewById(R.id.pref_spinner);
        areaSpinner = findViewById(R.id.area_spinner);

        JsonAsyncTask asyncTask = new JsonAsyncTask(this, dbm.getReadableDatabase());
        asyncTask.execute();

        setPrefSpinner();
    }

    public void reload() {
        String pref = (String)prefSpinner.getSelectedItem();
        String city = (String)areaSpinner.getSelectedItem();
//        Log.d("reload", pref + city);
        if(!pref.equals("都道府県") && !city.equals("市区町村")) {
            readData(pref, city);
        }

        try {
            final String TAG = "Earthquake";
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
            Log.d(TAG, "" + target.getCount());
//            for (int i = 0; i < target.getCount(); i++) {
            for (int i = 0; i < 1; i++) {
                String datetime = target.getString(1);
                String hypocenter = target.getString(2);
                double north_lat = target.getDouble(3);
                double east_long = target.getDouble(4);
                int depth = target.getInt(5);
                double magnitude = target.getDouble(6);
                String max_int = target.getString(7);
                String city_list = target.getString(8);
                String message = target.getString(9);

                sbuilder.append("\n");
                sbuilder.append("発生時刻: ").append(datetime).append("\n");
                sbuilder.append("震源: ").append(hypocenter).append("\n");
                sbuilder.append("北緯: ").append(north_lat).append(", 東経: ").append(east_long).append("\n");
                sbuilder.append("深さ: ").append(depth / 1000).append("km\n");
                sbuilder.append("マグニチュード: ").append(magnitude).append("\n");
                sbuilder.append(city_list).append("\n");
                sbuilder.append(message).append("\n");
                Log.d(TAG, sbuilder.toString());

                target.moveToNext();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
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
//        Log.d(TAG, target.getString(1) + " " + target.getString(2));
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
        textView.setText(sbuilder.toString());

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
                if(!selectedItem.equals("都道府県")) textView.setText(selectedItem);
                target.close();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                textView.setText("Hello World!");
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
                textView.setText("Hello World!");
            }
        });
    }

}
