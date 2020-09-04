package com.example.teamet.light_app.network;

import android.os.IBinder;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.KeyGenerator;

public class AES {
    public static String bin2Hex(byte[] data){
        StringBuffer sb = new StringBuffer();
        for(byte b : data){
            String s = Integer.toHexString(0xff & b);
            if(s.length() == 1){
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static byte[] hex2bin(String hex){
        byte[] bytes = new byte[hex.length()/2];
        for(int i=0; i<bytes.length; i++){
            bytes[i] = (byte)Integer.parseInt(hex.substring(i*2, (i+1)*2), 16);
        }
        return bytes;
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator KeyGen = KeyGenerator.getInstance("AES");
        KeyGen.init(128);
        return KeyGen.generateKey();
    }

    public static IvParameterSpec generateIV(){
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] encrypto(String plainText, SecretKey key, IvParameterSpec iv) throws GeneralSecurityException {
        Cipher encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
        return encrypter.doFinal(plainText.getBytes());
    }

    public static String decrypto(byte[] cryptoText, SecretKey key, IvParameterSpec iv) throws GeneralSecurityException {
        Cipher decrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decrypter.init(Cipher.DECRYPT_MODE, key, iv);
        return new String(decrypter.doFinal(cryptoText));
    }

}