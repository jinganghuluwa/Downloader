package com.tzc.downloadmanager.downloader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 字符串MD5加密工具类
 */
public class MD5Utils {
    /**
     * 获取字符串MD5加密结果
     *
     * @param string 要加密字符串
     * @return md5加密结果
     * @throws NoSuchAlgorithmException 没有找到md5加密类异常
     */
    public static String getMD5(String string) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(string.getBytes());
            byte[] m = md5.digest();//加密
            return getString(m);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 数组转字符串
     *
     * @param bytes 要转化的数组
     * @return 转化后的字符串
     */
    private static String getString(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            stringBuffer.append(bytes[i]);
        }
        return stringBuffer.toString();
    }
}
