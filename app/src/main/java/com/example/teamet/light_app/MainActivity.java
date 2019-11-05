package com.example.teamet.light_app;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Test_Database helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DB作成
        helper = new Test_Database(getApplicationContext());

        // 変数textViewに表示するテキストビューのidを格納
        textView = findViewById(R.id.text_view);
    }

    /**
     * DBからデータを全件取得し画面に表示する.
     * @param view
     */
    public void readData(View view){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "testdb",
                new String[] { "title", "score" },
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append(":    ");
            sbuilder.append(cursor.getInt(1));
            sbuilder.append("点\n\n");
            cursor.moveToNext();
        }

        cursor.close();

        textView.setText(sbuilder.toString());
    }

    /**
     * データを保存する.
     * @param view
     */
    public void saveData(View view) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        EditText editTextTitle = findViewById(R.id.edit_text_key);
        EditText editTextScore = findViewById(R.id.edit_text_value);
        String title = editTextTitle.getText().toString();
        String score = editTextScore.getText().toString();
        values.put("title", title);
        values.put("score", score);

        db.insert("testdb", null, values);
    }
}
