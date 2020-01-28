package com.example.teamet.light_app;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.Toast;

import java.io.*;
import java.net.*;

public class Client extends AsyncTask<String, Void, String> {

    public static String HOST;
    public static int PORT;

    private Socket sc;
    private BufferedReader br;
    private PrintWriter pw;
    private EditText et;
    private EditText ip;
    private EditText port;
    private Context context;
    private int duration = Toast.LENGTH_SHORT;
    private Toast toast;
    private File json;
    private FileReader fr;
    private BufferedReader jsonBR;

    public Client(EditText editText, EditText ipText, EditText portText, Context co){
        et = editText;
        ip = ipText;
        port = portText;
        context = co;
    }

    protected String doInBackground(String... cx){
        Connect();
        return "success";
    }

    protected  void onPostExecute(String result){
        toast = Toast.makeText(context, "送信成功", duration);
        toast.show();
    }

    public void Connect(){
        HOST = ip.getText().toString();
        String tmp = port.getText().toString();
        PORT = Integer.parseInt(tmp);
        try {
            sc = new Socket(HOST, PORT);
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

            json = new File("assets\\data.json");
            fr = new FileReader(json);
            jsonBR = new BufferedReader(fr);
            String str = jsonBR.readLine();//送信するjsonファイルが1行のみである前提で1行しか読み込ませない

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            pw.println(str);
            pw.flush();
        }catch (UnknownHostException e){
            toast = Toast.makeText(context, "ホストが特定できませんでした", duration);
            toast.show();
            e.printStackTrace();
        }catch(Exception e){
            toast = Toast.makeText(context, "入出力中にエラーが発生しました: " + e.toString(), duration);
            toast.show();
            e.printStackTrace();
        }
    }
}
