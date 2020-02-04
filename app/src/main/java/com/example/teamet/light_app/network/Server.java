package com.example.teamet.light_app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.util.Consumer;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends AsyncTask<String, Void, Void> {
    public final int BUF_SIZE = 1024;

    private Context co;
    private int duration = Toast.LENGTH_SHORT;
    private Socket sc;
    private BufferedReader br;
    private char[] buf;
    private ArrayList<InetAddress> clients;
    private P2pManager pm;

    public Server(String port, Context context, P2pManager pm){
        co = context;
        buf = new char[BUF_SIZE];
        clients = new ArrayList<>();
        this.pm = pm;
    }

    protected Void doInBackground(String... cx){
        Log.v("Server", "Starting server...");
        accept();
        return null;
    }

    private String recv() throws IOException {
        int lenRecved;
        StringBuilder sb = new StringBuilder();

        do {
            lenRecved = br.read(buf, 0, BUF_SIZE);
            if (lenRecved != -1) {
                sb.append(buf, 0, lenRecved);
            }
        } while (lenRecved == BUF_SIZE);

        return sb.toString();
    }

    public void saveJson(String data){
        try {
            Log.v("Server", "Saving JSON...");
            PrintWriter pw = new PrintWriter("assets\\data.json", "UTF-8");
            pw.print(data);
            pw.close();
            Log.v("Server", "Saved JSON.");
        }catch(IOException e){
            Log.v("Server", "Failed saving JSON: " + e.toString());
            e.printStackTrace();
        }
    }

    public void accept(){
        try{
            ServerSocket ss = new ServerSocket(Router.PORT);

            Log.v( "Server", String.format("Listening on %s:%d ...", ss.getInetAddress().toString(), ss.getLocalPort()) );

            while(true){
                try{
                    sc = ss.accept();
                    Log.v( "Server", String.format("Connection from %s:%d", sc.getInetAddress().toString(), sc.getPort()) );

                    br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

                    String data = recv();
                    if (data.length() == 0) {
                        Log.v("Server", "Received empty data.");
                        if (!clients.contains(sc.getInetAddress())) {
                            clients.add(sc.getInetAddress());
                        }

                        Log.v("Server", "Current client list: " + clients.toString());

                        sendClients();
                    }
                    else {
                        Log.v("Server", "Received non-empty data.");
                        saveJson(data);
                    }

                    br.close();
                    sc.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void sendClients() {
        Log.v("Server", "Group-owner is me.");
        for (InetAddress addr : clients) {
            Client client = new Client(addr, String.valueOf(Router.PORT), co);
            client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    public void sendJsonFile(){
        Log.v("Client", "Sending JSON file...");
        try {
            File json = new File("assets\\data.json");
            BufferedReader jsonBR = new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8"));
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

    public void send(String data){
        try{
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            pw.write(data);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
