package com.example.teamet.light_app;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;


public class Test_Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_NAME = "TestDB.db";
    private static final String TABLE_NAME = "testdb";
    private static final String _ID = "_id";
    private static final String COLUMN_NAME_TITLE = "title";
    private static final String COLUMN_NAME_SUBTITLE = "score";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TITLE + " TEXT," +
                    COLUMN_NAME_SUBTITLE + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    Test_Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                SQL_CREATE_ENTRIES
        );

        saveData(db, "music1", 10);
        saveData(db, "music2", 0);
        saveData(db, "music3", 0);
        saveData(db, "music4", 0);
        saveData(db, "music5", 0);

    }

    // 参考：https://sankame.github.io/blog/2017-09-05-android_sqlite_db_upgrade/

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void saveData(SQLiteDatabase db, String title, int score) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("score", score);

        db.insert("testdb", null, values);
    }
}