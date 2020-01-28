package com.example.teamet.light_app.source;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.example.teamet.light_app.DisplayInfoActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;

public class JsonAsyncTask extends AsyncTask<Void, Void, Void> {

    private JsonTask jsonTask;

    public JsonAsyncTask(SQLiteDatabase db) {
        super();
        this.jsonTask = new JsonTask(db);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        jsonTask.run();
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("ss");
            int now = Integer.parseInt(sdf.format(calendar.getTime()));
            Log.v("JAT", ""+now);
            Thread.sleep((60 - now) * 1000);
        } catch(Exception e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        timer.schedule(this.jsonTask, 5000, 60000);
        return null;
    }
}
