package me.gavin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具
 *
 * @author gavin.xiong 2017/3/3
 */
public class MD5 {

    /**
     * 字符串 MD5 加密
     */
    public static String md5(String string) {
        if (string == null) {
            return "";
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xFF);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串 SHA-1 加密
     */
    public static String sha1(String string) {
        if (string == null) {
            return "";
        }
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte bytes[] = sha1.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xFF);
                if (temp.length() < 2) {
                    result += "0";
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
