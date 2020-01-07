package com.example.teamet.light_app;

import android.os.AsyncTask;
import android.widget.Toast;

import java.net.*;
import java.util.*;

public class Server extends AsyncTask<String, Void, String> {
    public static final int PORT = 0;

    private Toast toast;

    protected String doInBackground(String... cx){
        return null;
    }

    protected  void onPostExecute(String result){

    }

    public void connect(){
        Server server = new Server();
        try{
            InetAddress addr = InetAddress.getLocalHost();
        }catch(UnknownHostException e){
            e.printStackTrace();
        }
    }
}
