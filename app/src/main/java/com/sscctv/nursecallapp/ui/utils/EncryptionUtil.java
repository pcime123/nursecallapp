package com.sscctv.nursecallapp.ui.utils;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class EncryptionUtil {
    private static final String TAG = "EncryptionUtil";

    private static String IV;
    private final static String secretKey = "1435276533124653";

    private static volatile EncryptionUtil INSTANCE;

    public static EncryptionUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (EncryptionUtil.class) {
                if (INSTANCE == null) INSTANCE = new EncryptionUtil();
            }
        }
        return INSTANCE;
    }

    private EncryptionUtil() {
        IV = secretKey.substring(0, 16);
    }

    public static String encode(String str) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Log.i(TAG, "encode Write: " + str);

        byte[] data = secretKey.getBytes();
        SecretKey secureKey = new SecretKeySpec(data, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));
        byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
        Log.i(TAG, "encode: " + Arrays.toString(Base64.encodeBase64(encrypted)));

        return new String(Base64.encodeBase64(encrypted));
    }

    public static String decode(String str) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        byte[] data = secretKey.getBytes();
        SecretKey secureKey = new SecretKeySpec(data, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));
        byte[] byteStr = Base64.decodeBase64(str.getBytes());
        return new String(cipher.doFinal(byteStr), StandardCharsets.UTF_8);
    }

    public static String getPassword() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PASSWORD);
        String str;
        String pw = null;
        FileReader fileReader;
        BufferedReader bufferedReader;
        if (file.exists()) {
            try {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                while ((str = bufferedReader.readLine()) != null) {
                    pw = decode(str);
                }
                bufferedReader.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return pw;
    }
}
