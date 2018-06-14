package com.cetcme.xkterminal.util;

import android.content.Context;
import android.text.TextUtils;

import com.cetcme.xkterminal.MyClass.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class AisUtils {
    private static int serialNo = 0;
    private static final String keyJsonStr = "{\"000000\":\"0\",\"000001\":\"1\",\"000010\":\"2\",\"000011\":\"3\",\"000100\":\"4\",\"000101\":\"5\",\"000110\":\"6\",\"000111\":\"7\",\"001000\":\"8\",\"001001\":\"9\",\"001010\":\":\",\"001011\":\";\",\"001100\":\"<\",\"001101\":\"=\",\"001110\":\">\",\"001111\":\"?\",\"010000\":\"@\",\"010001\":\"A\",\"010010\":\"B\",\"010011\":\"C\",\"010100\":\"D\",\"010101\":\"E\",\"010110\":\"F\",\"010111\":\"G\",\"011000\":\"H\",\"011001\":\"I\",\"011010\":\"J\",\"011011\":\"K\",\"011100\":\"L\",\"011101\":\"M\",\"011110\":\"N\",\"011111\":\"O\",\"100000\":\"P\",\"100001\":\"Q\",\"100010\":\"R\",\"100011\":\"S\",\"100100\":\"T\",\"100101\":\"U\",\"100110\":\"V\",\"100111\":\"W\",\"101000\":\"`\",\"101001\":\"a\",\"101010\":\"b\",\"101011\":\"c\",\"101100\":\"d\",\"101101\":\"e\",\"101110\":\"f\",\"101111\":\"g\",\"110000\":\"h\",\"110001\":\"i\",\"110010\":\"j\",\"110011\":\"k\",\"110100\":\"l\",\"110101\":\"m\",\"110110\":\"n\",\"110111\":\"o\",\"111000\":\"p\",\"111001\":\"q\",\"111010\":\"r\",\"111011\":\"s\",\"111100\":\"t\",\"111101\":\"u\",\"111110\":\"v\",\"111111\":\"w\"}";
    private static final String contentJsonStr = "{\"@\":\"000000\",\"A\":\"000001\",\"B\":\"000010\",\"C\":\"000011\",\"D\":\"000100\",\"E\":\"000101\",\"F\":\"000110\",\"G\":\"000111\",\"H\":\"001000\",\"I\":\"001001\",\"J\":\"001010\",\"K\":\"001011\",\"L\":\"001100\",\"M\":\"001101\",\"N\":\"001110\",\"O\":\"001111\",\"P\":\"010000\",\"Q\":\"010001\",\"R\":\"010010\",\"S\":\"010011\",\"T\":\"010100\",\"U\":\"010101\",\"V\":\"010110\",\"W\":\"010111\",\"X\":\"011000\",\"Y\":\"011001\",\"Z\":\"011010\",\"[\":\"011011\",\"\\\\\":\"011100\",\"]\":\"011101\",\"^\":\"011110\",\"_\":\"011111\",\" \":\"100000\",\"!\":\"100001\",\"\\\"\":\"100010\",\"#\":\"100011\",\"$\":\"100100\",\"%\":\"100101\",\"&\":\"100110\",\"'\":\"100111\",\"(\":\"101000\",\")\":\"101001\",\"*\":\"101010\",\"+\":\"101011\",\",\":\"101100\",\"-\":\"101101\",\".\":\"101110\",\"/\":\"101111\",\"0\":\"110000\",\"1\":\"110001\",\"2\":\"110010\",\"3\":\"110011\",\"4\":\"110100\",\"5\":\"110101\",\"6\":\"110110\",\"7\":\"110111\",\"8\":\"111000\",\"9\":\"111001\",\":\":\"111010\",\";\":\"111011\",\"<\":\"111100\",\"=\":\"111101\",\">\":\"111110\",\"?\":\"111111\"}";
    private static JSONObject keyMap = null;
    private static JSONObject contentMap = null;

    static {
        try {
            keyMap = new JSONObject(keyJsonStr);
            contentMap = new JSONObject(contentJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String create14Msg(Context context, String message) {
        String headStr = "!AIVDO,1,1," + (serialNo++) + ",A,";
        StringBuffer sb = new StringBuffer("00111000");
        // 消息ID
        String mmsi = PreferencesUtils.getString(context, "shipNo", "");
        int bu = 30;
        if (!"".equals(mmsi)) {
            // MMSI
            String mmsiByts = Integer.toBinaryString(Integer.valueOf(mmsi));
            bu = 30 - mmsiByts.length();//30位补0
            for (int i = 0; i < bu; i++) {
                sb.append("0");
            }
            sb.append(mmsiByts);
        } else {
            for (int i = 0; i < bu; i++) {
                sb.append("0");
            }
        }
        //备用
        sb.append("00");
        if (!TextUtils.isEmpty(message)) {
            message = message.toUpperCase();
            for (int i = 0; i < message.length(); i++) {
                try {
                    sb.append(contentMap.getString(message.charAt(i) + ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //比特总数=
        int v = 0;
        int len = sb.toString().length();
        if (len % 6 != 0) {
            v = 6 - len % 6;
            for (int i = 0; i < v; i++) {
                sb.append("0");
            }
        }
        String finalStr = sb.toString();
        len = finalStr.length();
        int ck = 0;
        for (int i = 0; i < len; i += 6) {
            String ss = "";
            for (int j = i; j < i + 6; j++) {
                ss += finalStr.charAt(j);
            }
            try {
                headStr += keyMap.getString(ss);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //校验和
        headStr += "," + v;
        len = headStr.length();
        for (int i = 1; i < len; i++) {
            ck ^= headStr.charAt(i);
        }
        String last = "*" + Integer.toHexString(ck);
        headStr += last;
        return headStr;
    }
}
