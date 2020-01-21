package com.example.teamet.light_app;

import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.nio.Buffer;

public class ConnectToClient extends Thread {
    private Socket sc;
    private PrintWriter pw;
    private BufferedReader br;
    private TextView result;

    public ConnectToClient(Socket s, TextView tx){
        sc = s;
        result = tx;
    }

    public void getJSON(BufferedReader br, FileWriter fileWriter){
        try{
            String str = br.readLine();
            fileWriter.write(str);
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

    public void run(){
        try{
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
        }catch(Exception e){
            e.printStackTrace();
        }

        File json = new File("app\\assets\\data.json");

        while(true){
            try{
                FileWriter fileWriter = new FileWriter(json);
                getJSON(br, fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
