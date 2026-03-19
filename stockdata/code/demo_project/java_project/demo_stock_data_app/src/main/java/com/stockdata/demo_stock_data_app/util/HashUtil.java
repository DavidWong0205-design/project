package com.stockdata.demo_stock_data_app.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static String makeId(String symbol, String timerange, String date) {
        try {
            String key = symbol + "_" + timerange + "_" + date;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(key.getBytes());

            // 轉成 32 字元的十六進制字串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // 固定 32 字元
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}