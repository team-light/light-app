package com.example.teamet.light_app;

import android.widget.TextView;

import java.io.*;
import java.net.*;

public class ConnectToClient extends Thread {
    private Socket sc;
    private PrintWriter pw;
    private BufferedReader br;
    private TextView result;

    public ConnectToClient(Socket s, TextView tx){
        sc = s;
        result = tx;
    }

    public void getJSON(BufferedReader br, PrintWriter pw){
        try {
            String data = br.readLine();
            pw.println(data);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void run(){
        try{
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
        }catch(Exception e){
            e.printStackTrace();
        }

        while(true){
            try{
                PrintWriter jsonPW = new PrintWriter("assets\\data.json", "UTF-8");
                getJSON(br, jsonPW);
                jsonPW.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
