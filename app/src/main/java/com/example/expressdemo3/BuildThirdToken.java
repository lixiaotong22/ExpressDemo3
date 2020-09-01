package com.example.expressdemo3;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;

import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BuildThirdToken{
    static final private int IV_LENGTH = 16;

    static final private String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static byte[] encrypt(String content, byte[] secretKey) throws Exception{
        SecretKeySpec key = new SecretKeySpec(secretKey, "AES");

        Random rnd = new Random();
        byte[] ivBytes = new byte[IV_LENGTH];
        rnd.nextBytes(ivBytes);
        System.out.println(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] contentBytes = cipher.doFinal(content.getBytes("UTF-8"));
        byte[] encryptedBytes = new byte[ivBytes.length + contentBytes.length];

        System.arraycopy(ivBytes, 0, encryptedBytes, 0, ivBytes.length);
        System.arraycopy(contentBytes, 0, encryptedBytes, ivBytes.length, contentBytes.length);

        return Base64.getEncoder().encode(encryptedBytes);
    }

    public static void main(String[] args) throws Exception{

        Long timestamp = System.currentTimeMillis()/1000 + 3600;
        byte[] mServerSecret = "12345678123456781234567812345678".getBytes(); // Secret联系zego技术支持

        JSONObject encryptResult = new JSONObject();
        encryptResult.put("app_id", 123456789); // 数值型, appid联系zego技术支持
        encryptResult.put("timeout", timestamp); // 数值型, 注意必须是当前时间戳(秒)加超时时间(秒)
        encryptResult.put("nonce", 11111111); // 随机数,须为数值型
        encryptResult.put("id_name", "hsx2117");// 字符串,id_name必须跟setUser的userid相同
        byte[] encryptByte = BuildThirdToken.encrypt(encryptResult.toString(), mServerSecret);

        String encryptContent = "01" + new String(encryptByte, "utf-8");//最后结果须加version("01")为前缀
        System.out.println(encryptContent);
    }

}

