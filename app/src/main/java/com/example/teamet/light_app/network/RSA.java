package com.example.teamet.light_app.network;

import java.security.*;
import javax.crypto.Cipher;

public class RSA {
    public RSA(){}

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator KeyGen = KeyPairGenerator.getInstance("RSA");
        KeyGen.initialize(1024);
        return KeyGen.generateKeyPair();
    }

    public static byte[] encrypto(String plainText, PublicKey publickey) throws GeneralSecurityException{
        Cipher encrypter = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encrypter.init(Cipher.ENCRYPT_MODE, publickey);
        return encrypter.doFinal(plainText.getBytes());
    }

    public static String decrypto(byte[] cryptoText, PrivateKey privatekey) throws GeneralSecurityException{
        Cipher decrypter = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decrypter.init(Cipher.DECRYPT_MODE, privatekey);
        return new String(decrypter.doFinal(cryptoText));
    }
}
