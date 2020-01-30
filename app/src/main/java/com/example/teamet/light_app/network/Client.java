package com.example.teamet.light_app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<String, Void, String> {
    public static String HOST;
    public static int PORT;

    private Socket sc;
    private BufferedReader br;
    private PrintWriter pw;
    private InetAddress addr;
    private String port;
    private Context context;
    private int duration = Toast.LENGTH_SHORT;
    private Handler handler;
    private Toast toast;
    private File json;
    private BufferedReader jsonBR;

    public Client(InetAddress addr, String port, Context co){
        this.addr = addr;
        this.port = port;
        context = co;
        handler = new Handler();
    }

    protected String doInBackground(String... cx) {
        Connect();
        return null;
    }

    protected  void onPostExecute(String result){
        //toast = Toast.makeText(context, "送信成功", duration);
        //toast.show();
    }

    public void sendJsonFile(){
        Log.v("Client", "Sending JSON file...");
        try {
//            json = new File("assets\\data.json");
//            jsonBR = new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8"));
            jsonBR = new BufferedReader(new InputStreamReader(context.openFileInput("data.json"), "UTF-8"));
            String str = jsonBR.readLine();//送信するjsonファイルが1行のみである前提で1行しか読み込ませない
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            pw.println(str);
            pw.flush();
        }catch(IOException e){
            //toast = Toast.makeText(context, "ファイルの入出力中にエラーが発生しました"+e.toString(), duration);
            //toast.show();
            Log.v("Client", e.toString());
            e.printStackTrace();
        }
    }

    public void Connect(){
        HOST = addr.toString();
        PORT = Integer.parseInt(port);
        try {
            sc = new Socket(HOST, PORT);
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            sendJsonFile();
        }catch (UnknownHostException e){
            //toast = Toast.makeText(context, "ホストが特定できませんでした", duration);
            //toast.show();
            Log.v("Client", e.toString());
            e.printStackTrace();
        }catch(Exception e){
            //toast = Toast.makeText(context, "入出力中にエラーが発生しました: " + e.toString(), duration);
            //toast.show();
            Log.v("Client", e.toString());
            e.printStackTrace();
        }
    }

    private void showToast(final String text) {
        execMainLooper(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
                Log.v("toast:", text);
            }
        });
    }

    private void execMainLooper(Runnable runnable) {
        handler.post(runnable);
    }
}
