package com.example.teamet.light_app;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;

public class Server extends AsyncTask<String, Void, String> {
    public static final int PORT = 0;

    private Toast toast;
    private TextView ip;
    private TextView portT;
    private TextView status;
    private TextView result;
    private Context co;
    private int duration = Toast.LENGTH_SHORT;

    public Server(TextView addr, TextView Port, TextView st, TextView res, Context context){
        ip = addr;
        portT = Port;
        co = context;
        result = res;
        status = st;
    }

    protected String doInBackground(String... cx){
        connect();
        return null;
    }

    protected void onPostExecute(String result){

    }

    public void saveJson(String data){
        try {
            PrintWriter pw = new PrintWriter("assets\\data.json", "UTF-8");
            pw.print(data);
            pw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void connect(){
        try{
            ServerSocket ss = new ServerSocket(PORT);
            portT.setText(String.valueOf(ss.getLocalPort()));
            status.setText(R.string.connecting);
            while(true){
                try{
                    Socket sc = ss.accept();
                    status.setText(R.string.connect);
                    ConnectToClient cc = new ConnectToClient(sc, result);
                    cc.start();
                }catch (Exception ex){
                    toast = Toast.makeText(co, ex.toString(), duration);
                    toast.show();
                }
            }
        }catch (IOException e){
            toast = Toast.makeText(co, e.toString(), duration);
            toast.show();
        }
    }
}
