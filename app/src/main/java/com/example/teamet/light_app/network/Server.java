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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Server extends AsyncTask<String, Void, Void> {
    public final int BUF_SIZE = 1024;

    private Context co;
    private int duration = Toast.LENGTH_SHORT;
    private Socket sc;
    private BufferedReader br;
    private char[] buf;
    private ArrayList<InetAddress> clients;
    private P2pManager pm;
    private double prevRequestedTime = Double.NEGATIVE_INFINITY;
    private boolean alive;

    private Map<Byte, Integer> countByMethod;

    public Server(Context context, P2pManager pm){
        co = context;
        buf = new char[BUF_SIZE];
        clients = new ArrayList<>();
        this.pm = pm;
        countByMethod = new HashMap<>();
    }

    protected Void doInBackground(String... cx){
        Log.v("Server", "Starting server...");
        accept();
        return null;
    }

    public void stop() {
        alive = false;
    }

    public void logStatistics() {
        Integer count;

        Log.v("Server", "count of request:");
        if (countByMethod.containsKey(Router.METHOD_GET)) {
            Log.v("Server", "GET: " + countByMethod.get(Router.METHOD_GET));
        }
        if (countByMethod.containsKey(Router.METHOD_POST)) {
            Log.v("Server", "POST: " + countByMethod.get(Router.METHOD_POST));
        }
        if (countByMethod.containsKey(Router.METHOD_POLL_LOAD)) {
            Log.v("Server", "POLL_LOAD: " + countByMethod.get(Router.METHOD_POLL_LOAD));
        }
    }

    public void accept(){
        alive = true;
        try {
            ServerSocket ss = new ServerSocket(Router.PORT);

            Log.v( "Server", String.format("Listening on %s:%d ...", ss.getInetAddress().toString(), ss.getLocalPort()) );

            while(alive){
                try{
                    sc = ss.accept();
                    Log.v( "Server", String.format("Connection from %s:%d", sc.getInetAddress().toString(), sc.getPort()) );

                    br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

                    String data = Router.recv(br, buf);

                    byte method = data.getBytes()[0];
                    String text = new String(data.getBytes("UTF-8"), 1, data.getBytes().length - 1, "UTF-8");

                    if (countByMethod.containsKey(method)) {
                        countByMethod.put(method, countByMethod.get(method)+1);
                    }
                    else {
                        countByMethod.put(method, 1);
                    }

                    switch (method) {
                        case Router.METHOD_POST:
                            Log.v("Server", "Received POST request.");
                            prevRequestedTime = System.currentTimeMillis();
                            Router.saveJson(text, co);
                            break;

                        case Router.METHOD_GET:
                            Log.v("Server", "Received GET request.");
                            prevRequestedTime = System.currentTimeMillis();
                            Router.sendJsonFile(sc, false, co);
                            break;

                        case Router.METHOD_POLL_LOAD:
                            Log.v("Server", "Received POLL-LOAD request.");
                            Router.sendData(sc, String.valueOf(calcLoad()), null);
                            break;

                        default:
                            Log.v("Server", "Received unknown method request: " + method);
                    }

                    if (!clients.contains(sc.getInetAddress())) {
                        clients.add(sc.getInetAddress());
                    }
                    Log.v("Server", "Current client list: " + clients.toString());

                    br.close();
                    sc.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    double calcLoad() {
        return 1000.0 / (System.currentTimeMillis() - prevRequestedTime);
    }
}
