package com.example.teamet.light_app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.util.Consumer;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.util.ArrayList;

public class Server extends AsyncTask<String, Void, Void> {
    public final int BUF_SIZE = 1024;

    private Context co;
    private int duration = Toast.LENGTH_SHORT;
    private SSLSocket sc;
    private SSLSocketFactory fact;
    private SSLServerSocketFactory ssFact;
    private Handler handler;
    private BufferedReader br;
    private char[] buf;
    private ArrayList<InetAddress> clients;
    private P2pManager pm;

    public Server(String port, Context context, P2pManager pm){
        co = context;
        handler = new Handler();
        buf = new char[BUF_SIZE];
        clients = new ArrayList<>();
        this.pm = pm;
    }

    protected Void doInBackground(String... cx){
        accept();
        return null;
    }

    private String recv() throws IOException {
        int lenRecved;
        StringBuilder sb = new StringBuilder();

        do {
            lenRecved = br.read(buf, 0, BUF_SIZE);
            sb.append(buf, 0, lenRecved);
        } while (lenRecved == BUF_SIZE);

        return sb.toString();
    }

    public void saveJson(String data){
        try {
            FileOutputStream fop = co.openFileOutput("data.json", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fop);
            osw.write(data);
            osw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void accept(){
        try{
            ssFact = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            SSLServerSocket ss = (SSLServerSocket)ssFact.createServerSocket (Router.PORT);
            fact = (SSLSocketFactory)SSLSocketFactory.getDefault();
            sc = (SSLSocket)fact.createSocket();
            while(true){
                try{
                    sc = (SSLSocket) ss.accept();
                    br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

                    String data = recv();
                    if (data.length() == 0) {
                        clients.add( sc.getInetAddress() );
                    }
                    else {
                        saveJson(data);

                        pm.requestIsGroupOwner(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean isGroupOwner) {
                                if (isGroupOwner) {
                                    for (InetAddress addr : clients) {
                                        Client client = new Client(addr, String.valueOf(Router.PORT), co);
                                        client.execute();
                                    }
                                }
                            }
                        });
                    }

                    br.close();
                    sc.close();
                } catch (Exception ex){
                    showToast(ex.toString());
                }
            }
        }catch (IOException e){
            showToast(e.toString());
        }
    }

    public void send(String data){
        try{
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            pw.write(data);
        }catch(Exception e){
            showToast("送信できませんでした" + e.toString());
            e.printStackTrace();
        }
    }

    private void showToast(final String text) {
        execMainLooper(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(co, text, duration).show();
                Log.v("toast:", text);
            }
        });
    }

    private void execMainLooper(Runnable runnable) {
        handler.post(runnable);
    }
}
