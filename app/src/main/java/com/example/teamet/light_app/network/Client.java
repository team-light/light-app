package com.example.teamet.light_app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
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

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client extends AsyncTask<String, Void, String> {
    public static String HOST;
    public static int PORT;

    private SSLSocket sc;
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
    private SSLSocketFactory fact;
    private SecretKey key;
    private IvParameterSpec iv;

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

    public void sendJsonFile(PrintWriter pw){
        try {
            json = new File("assets\\data.json");
            jsonBR = new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8"));
            String str = jsonBR.readLine();//送信するjsonファイルが1行のみである前提で1行しか読み込ませない
            pw.println(AES.bin2Hex(AES.encrypto(str, key, iv)));
            pw.flush();
        }catch(IOException e){
            //toast = Toast.makeText(context, "ファイルの入出力中にエラーが発生しました"+e.toString(), duration);
            //toast.show();
            e.printStackTrace();
        }catch(GeneralSecurityException gse){
            gse.printStackTrace();
        }
    }

    private void exchangeKeys(BufferedReader br, PrintWriter pw){
        try {
            KeyPair keypair = RSA.generateKeyPair();
            PublicKey publickey = keypair.getPublic();
            PrivateKey privatekey = keypair.getPrivate();
            pw.println(Base64.encodeToString(publickey.getEncoded(), Base64.DEFAULT));
            pw.flush();

            byte[] secret = AES.hex2bin(br.readLine());
            byte[] ReadIv = AES.hex2bin(br.readLine());
            String decryptokey = RSA.decrypto(secret, privatekey);
            String decryptoiv = RSA.decrypto(ReadIv, privatekey);
            key = new SecretKeySpec(Base64.decode(decryptokey, Base64.DEFAULT), "AES");
            iv = new IvParameterSpec(Base64.decode(decryptoiv, Base64.DEFAULT));
        }catch(Exception e){}
    }

    public void Connect(){
        HOST = addr.toString();
        PORT = Integer.parseInt(port);
        try {
            fact = (SSLSocketFactory)SSLSocketFactory.getDefault();
            sc = (SSLSocket)fact.createSocket(HOST, PORT);
            br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
            exchangeKeys(br, pw);
            sendJsonFile(pw);
        }catch (UnknownHostException e){
            //toast = Toast.makeText(context, "ホストが特定できませんでした", duration);
            //toast.show();
            e.printStackTrace();
        }catch(Exception e){
            //toast = Toast.makeText(context, "入出力中にエラーが発生しました: " + e.toString(), duration);
            //toast.show();
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
