package com.payoda.smartlock.service;

import android.util.Base64;

import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native String getEncryptionCypher();
    public static native String getEncryptionKey();
    public static native String getEncryptionVector();

    private static final String cypherInstance = getEncryptionCypher();
    private static final String KEY = getEncryptionKey();
    private static final String VECTOR = getEncryptionVector();


    private static AESEncryption instance=null;

    public static AESEncryption getInstance(){
        if(instance==null){
            instance=new AESEncryption();
        }
        return instance;
    }

    public String encrypt(String stringToEncode, boolean isEncrypt) {
        if(!isEncrypt)
            return stringToEncode;
        try {
            String key = KEY;
            String IV = VECTOR;
            SecretKeySpec skeySpec = getKey(key);
            byte[] clearText = stringToEncode.getBytes("utf-8");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes());
            Cipher cipher = Cipher.getInstance(cypherInstance);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            String encryptedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
            Logger.d("### Encrypt " + encryptedValue);
            return encryptedValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String stringToDecode, boolean isDecrypt) {
        if(!isDecrypt)
            return stringToDecode;
        try {
            Logger.d("### stringToDecode decrypt : " + stringToDecode);
            String key = KEY;
            String IV = VECTOR;
            byte[] decode = Base64.decode(stringToDecode, 0);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes());
            SecretKeySpec skeySpec = getKey(key);
            Cipher cipher = Cipher.getInstance(cypherInstance);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
            byte[] ret = cipher.doFinal(decode);
            String decryptedValue = new String(ret, "utf-8");
            Logger.d("### Decrypt : " + decryptedValue);
            return decryptedValue;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.d("### Decrypt Exception " );
            Logger.e(e);
        }
        return stringToDecode;
    }

    private static SecretKeySpec getKey(String Key) throws UnsupportedEncodingException {
        int keyLength = 128;
        byte[] keyBytes = new byte[keyLength / 8];
        Arrays.fill(keyBytes, (byte) 0x0);
        byte[] passwordBytes = Key.getBytes("UTF-8");
        int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        return key;
    }
    
}
