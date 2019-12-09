package com.example.teamet.light_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private DataBaseMake dbm;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);
        dbm = new DataBaseMake(getApplicationContext());

        SQLiteDatabase db = dbm.getReadableDatabase();
        Cursor test = db.query(
                "alert_view",
                null,
                null,
                null,
                null,
                null,
                null
        );
        test.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();
        Log.d("TAG", ""+test.getCount());
        for(int i = 0; i < test.getCount(); i++){
            Log.d("TAG", test.getString(0));
            sbuilder.append(test.getString(0)+" ");
            sbuilder.append(test.getString(1)+" ");
            sbuilder.append(test.getString(2)+" ");
            sbuilder.append(test.getString(3) + "\n");
            test.moveToNext();
        }
        test.close();

        textView.setText(sbuilder.toString());

    }
}
