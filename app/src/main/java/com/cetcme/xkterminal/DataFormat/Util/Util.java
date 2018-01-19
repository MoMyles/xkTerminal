package com.cetcme.xkterminal.DataFormat.Util;

/**
 * Created by qiuhong on 17/01/2018.
 */

public class Util {

    /**
     * Byte转Bit
     */
    public static String byteToBit(byte b) {
        return "" +(byte)((b >> 7) & 0x1) +
                (byte)((b >> 6) & 0x1) +
                (byte)((b >> 5) & 0x1) +
                (byte)((b >> 4) & 0x1) +
                (byte)((b >> 3) & 0x1) +
                (byte)((b >> 2) & 0x1) +
                (byte)((b >> 1) & 0x1) +
                (byte)((b >> 0) & 0x1);
    }

    /**
     * Bit转Byte
     */
    public static byte BitToByte(String byteStr) {
        int re, len;
        if (null == byteStr) {
            return 0;
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {//4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }

    /**
     * byte[]去掉第一位
     */
    public static  void bytesRemoveFirst(byte[] bytes, int count) {
        for (int i = 0; i < count; i++) {
            bytes[i] = bytes[i + 1];
        }
    }

    public static int computeCheckSum(byte[] buf, int start, int end) {
        int sum = 0;
        for (int i = start; i < end; i++) {
            sum += (buf[i] + 256) % 256;
        }
        return (sum & 0xFF);
    }

    /**
     * 字符串前面补0
     */
    public static String addZeroString(String str, int length) {
        while(str.length() < length) {
            str = "0" + str;
        }
        return str;
    }

}
