package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;

import java.io.UnsupportedEncodingException;

/**
 * Created by qiuhong on 17/01/2018.
 */

public class AlertFormat {

    private static String alertHead = "$05";

    public static final String ALERT_END_SYMBOL = "\r\n";

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
    public static void main(String[] args) {

        byte[] frameData = format("00000010", "00001110");
        String[] unFormatStrings = unFormat(frameData);
        String mode = unFormatStrings[0];
        String type = unFormatStrings[1];
        System.out.println(mode);
        System.out.println(type);
    }

}
