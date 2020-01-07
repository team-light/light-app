package com.example.teamet.light_app;

import java.io.*;
import java.net.*;

public class ConnectToClient extends Thread {
    private Socket sc;
    private PrintWriter pw;
    private BufferedReader br;

    public ConnectToClient(Socket s){
        sc = s;
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
