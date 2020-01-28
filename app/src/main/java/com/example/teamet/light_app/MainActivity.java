package com.example.teamet.light_app;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private TextView eqTextView;
    private Spinner prefSpinner;
    private Spinner areaSpinner;
    private DataBaseMake dbm;
    private Button eqButton;
    private ConstraintLayout layout_alarm;
    private ConstraintLayout layout_earthquake;
    private enum LayoutState{
        EQ,
        ALARM
    }
    private LayoutState layoutState = LayoutState.ALARM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DB作成
        dbm = new DataBaseMake(getApplicationContext());


        // 変数textViewに表示するテキストビューのidを格納
        textView = findViewById(R.id.text_view);
        eqTextView = findViewById(R.id.textView);
        prefSpinner = findViewById(R.id.pref_spinner);
        areaSpinner = findViewById(R.id.area_spinner);
        eqButton = findViewById(R.id.eq_Button);
        layout_alarm = (ConstraintLayout) findViewById(R.id.layout_alarm);
        layout_earthquake = (ConstraintLayout) findViewById(R.id.layout_earthquake);



        JsonAsyncTask asyncTask = new JsonAsyncTask(this, dbm.getReadableDatabase());
        asyncTask.execute();
//        json_task = asynktask.getjsonTask();
        setPrefSpinner();
        eqButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(layoutState == LayoutState.ALARM){
                    layout_alarm.setVisibility(View.GONE);
                    layout_earthquake.setVisibility(View.VISIBLE);
                    layoutState = LayoutState.EQ;
                    readEqData();
                }else if (layoutState == LayoutState.EQ){
                    layout_earthquake.setVisibility(View.GONE);
                    layout_alarm.setVisibility(View.VISIBLE);
                    layoutState = LayoutState.ALARM;
                }
            }
        });
    }


    public void reload() {
        String pref = (String)prefSpinner.getSelectedItem();
        String city = (String)areaSpinner.getSelectedItem();
//        Log.d("reload", pref + city);
        if(!pref.equals("都道府県") && !city.equals("市区町村")) {
            readData(pref, city);
        }
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

            sbuilder.append("最大震度　").append(max_int).append("　マグニチュード　").append("M"+magnitude).append("\n発生時刻 : ").append(datetime).append("\n震源地　 : ")
                    .append(hypocenter).append("\n深さ　　 : ");
            if(depth==0) {
                sbuilder.append("ごく浅い\n");
            }else{
                sbuilder.append("約"+depth/1000+"km\n");
            }
            sbuilder.append(city_list+"\n");

            target.moveToNext();
        }
        target.close();
        eqTextView.setText(sbuilder.toString());
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
