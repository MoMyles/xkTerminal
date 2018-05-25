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


    }

}
