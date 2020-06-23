package com.example.teamet.light_app.network;

import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.net.ssl.SSLSocket;

public class ConnectToClient extends Thread {
    private SSLSocket sc;
    private PrintWriter pw;
    private BufferedReader br;
    private TextView result;

    public ConnectToClient(SSLSocket s, TextView tx){
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
