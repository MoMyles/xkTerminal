package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by qiuhong on 17/01/2018.
 */

public class AlertFormat {

    private static final String alertHead = "$05";

    private static final String ALERT_END_SYMBOL = "\r\n";

    public static String[] unFormat(byte[] frameData) {

        try {
            String head = new String(ByteUtil.subBytes(frameData, 0, 3), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte mode = frameData[3];
        byte type = frameData[4];
        String modeStr = Util.byteToBit(mode);
        String typeStr = Util.byteToBit(type);

        return new String[]{modeStr, typeStr};
    }

    public static byte[] format(String mode, String type) {
        byte[] bytes = alertHead.getBytes();

        byte[] modeBytes = new byte[]{Util.BitToByte(mode)};
        byte[] typeBytes = new byte[]{Util.BitToByte(type)};

        byte[] toCheckBytes = ByteUtil.byteMerger(modeBytes, typeBytes);
        int checkSum = Util.computeCheckSum(toCheckBytes, 0, toCheckBytes.length);
        byte[] checkSumBytes = ByteUtil.byteMerger("*".getBytes(), new byte[]{(byte) checkSum});
        checkSumBytes = ByteUtil.byteMerger(checkSumBytes, ALERT_END_SYMBOL.getBytes());

        bytes = ByteUtil.byteMerger(bytes, toCheckBytes);
        bytes = ByteUtil.byteMerger(bytes, checkSumBytes);
        return bytes;
    }

    public static String getStringType(String type) {
        String[] defaultTypes = new String[] {
                "沉船",
                "搁浅",
                "碰撞",
                "火灾",
                "风灾",
                "伤病",
                "抓扣",
                "故障"
        };

        if (type.length() != 8) return "";
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            if (type.substring(i, i + 1).equals("1")) list.add(defaultTypes[7 - i]);
        }

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            str.append(list.get(i));
            if (i != list.size() - 1) str.append("、");
        }

        return str.toString();
    }

    public static void main(String[] args) {
        String hStr = "21465449442C46542D313730332C304145362C303939422C453438332C354338392C2020343133323433312C33312C3436202840";
        byte[] bytes = ConvertUtil.hexStr2Bytes(hStr);
        String convertStr = new String(bytes);
        System.out.println("ok: " + convertStr);


        String hStr1 = "98009A420A02CA0A42323401025196302040800002020252AA9A6A00F0";
        byte[] bytes1 = ConvertUtil.hexStr2Bytes(hStr1);
        String convertStr1 = new String(bytes1);
        System.out.println("fail:" + convertStr1);



//        byte[] frameData = format((byte) 0x00, "00001110");
//        String[] unFormatStrings = unFormat(frameData);
//        String mode = unFormatStrings[0];
//        String type = unFormatStrings[1];
//        System.out.println(mode);
//        System.out.println(getStringType(type));

        /*byte[] bytes = new byte[] {
                0x24,
                0x52,
                0x35,
                0x31,
                0x31,
                0x31,
                0x37,
                0x33,
                0x37,
                0x35,
                0x35,
                0x02,
                0x00,
                0x2A,
                (byte) 0xA0,
                0x3B,
        };



        byte[] alertBytes = ByteUtil.subBytes(bytes, 11, 13);
        if (alertBytes[0] == 0x02 && alertBytes[1] == 0x00) {
            System.out.println("123123123123123");
        }*/

    }

}
