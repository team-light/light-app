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

    public void run(){
        try{
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
        }catch(Exception e){
            e.printStackTrace();
        }
        while(true){
            try{
                String str = br.readLine();
                result.setText(str);
            }catch(Exception e){
                try{
                    br.close();
                    pw.close();
                    sc.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
}
