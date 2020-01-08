package com.example.teamet.light_app;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.Timer;

public class JsonAsyncTask extends AsyncTask<Void, Void, Void> {

    private JsonTask jsonTask;

    public JsonAsyncTask(SQLiteDatabase db) {
        super();
        this.jsonTask = new JsonTask(db);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Timer timer = new Timer();
        timer.schedule(this.jsonTask, 10000, 60000);
        return null;
    }
}
