package com.cetcme.xkterminal.DataFormat.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Date;

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
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     * @param value
     *            要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes( int value )
    {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }
    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }

    /**
     * byte[]去掉第一位
     */
    public static  void bytesRemoveFirst(byte[] bytes, int count) {
        for (int i = 0; i < count - 1; i++) {
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
    public static String stringAddZero(String str, int length) {
        while(str.length() < length) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * 字符串去掉前面的0
     */
    public static String stringRemoveZero(String str) {
        while (str.charAt(0) == '0' && str.length() > 1) {
            str = str.substring(1, str.length());
        }
        return str;
    }

    public static String bytesGetHead(byte[] bytes, int size) {
        if (bytes.length < size) return null;

        byte[] newBytes = new byte[size];
        System.arraycopy(bytes, 0, newBytes, 0, size);
        try {
            return new String(newBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String idCardGetSex(String idCardNumber) {
        if (idCardNumber.length() != 18) return "";

        int sexNumber = Integer.parseInt(idCardNumber.charAt(16)+ "");
        if (sexNumber % 2 == 0) {
            return "女";
        } else {
            return "男";
        }
    }

    public static String idCardGetBirthday(String idCardNumber) {
        if (idCardNumber.length() != 18) return "";

        String year = idCardNumber.substring(6, 10);
        String month = idCardNumber.substring(10, 12);
        String day = idCardNumber.substring(12, 14);
        return year + "年" + month + "月" + day + "日";
    }

    public static void main(String[] args) {
//        System.out.println(idCardGetBirthday("330238198811240134"));
//        System.out.println(ConvertUtil.bytesToHexString("12345678".getBytes()));


//        SharedPreferences sharedPreferences = getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
//        String lastSendTime = sharedPreferences.getString("lastSendTime", "");
//        Long sendDate = DateUtil.parseStringToDate("2018/01/23 16:53:00", DateUtil.DatePattern.YYYYMMDDHHMMSS).getTime();
//        Long now = new Date().getTime();
//        System.out.println(now - sendDate);


//        System.out.println(DateUtil.parseDateToString(new Date(), DateUtil.DatePattern.YYYYMMDDHHMMSS));

        byte[] bytes = "$R112345678".getBytes();

        byte[] timeBytes = new byte[] {
                0x12,
                0x01,
                0x0B,
                0x07,
                0x12,
                0x01
        };

        byte[] numberBytes = new byte[] {
                (byte) 0x00,
                (byte) 0x26,
                (byte) 0x12,
                (byte) 0x22
        };
        bytes = ByteUtil.byteMerger(bytes, timeBytes);
        bytes = ByteUtil.byteMerger(bytes, numberBytes);

//        try {
//            year = new String(ByteUtil.subBytes(bytes, 11, 12), "UTF-8");
//            String month = new String(ByteUtil.subBytes(bytes, 12, 13), "UTF-8");
//            String day = new String(ByteUtil.subBytes(bytes, 13, 14), "UTF-8");
//            String hour = new String(ByteUtil.subBytes(bytes, 14, 15), "UTF-8");
//            String minute = new String(ByteUtil.subBytes(bytes, 15, 16), "UTF-8");
//            String second = new String(ByteUtil.subBytes(bytes, 17, 18), "UTF-8");
//            String date = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
//            System.out.println("date: " + date);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        int year = ByteUtil.subBytes(bytes, 11, 12)[0]  & 0xFF;
        int month = ByteUtil.subBytes(bytes, 12, 13)[0]  & 0xFF;
        int day = ByteUtil.subBytes(bytes, 13, 14)[0]  & 0xFF;
        int hour = ByteUtil.subBytes(bytes, 14, 15)[0]  & 0xFF;
        int minute = ByteUtil.subBytes(bytes, 15, 16)[0]  & 0xFF;
        int second = ByteUtil.subBytes(bytes, 16, 17)[0]  & 0xFF;
        String date = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
        System.out.println("date: " + date);

        Date date1 = DateUtil.parseStringToDate(date);
        System.out.println(date1);


        System.out.println(bytesToInt2(ByteUtil.subBytes(bytes, 17, 21), 0));
    }

}
