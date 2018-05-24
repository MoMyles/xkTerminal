package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qiuhong on 14/05/2018.
 */

public class IDFormat {

    private static final String messageHead = "$02";

    private static final String MESSAGE_END_SYMBOL = "\r\n";

    public static String unFormat(byte[] frameData) {

        try {
            String head = new String(ByteUtil.subBytes(frameData, 0, 3), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new String(ByteUtil.subBytes(frameData, 3, 11));
    }

    public static byte[] format(String id) {
        byte[] bytes = messageHead.getBytes();
        byte[] addressBytes = id.getBytes();

        int checkSum = Util.computeCheckSum(addressBytes, 0, addressBytes.length);
        byte[] checkSumBytes = ByteUtil.byteMerger("*".getBytes(), new byte[]{(byte) checkSum});
        checkSumBytes = ByteUtil.byteMerger(checkSumBytes, MESSAGE_END_SYMBOL.getBytes());

        bytes = ByteUtil.byteMerger(bytes, addressBytes);
        bytes = ByteUtil.byteMerger(bytes, checkSumBytes);
        return bytes;
    }

    public static byte[] getID() {
        byte[] bytes = "$03".getBytes();
        byte[] addressBytes = new byte[] {0x01, 0x00};

        int checkSum = Util.computeCheckSum(addressBytes, 0, addressBytes.length);
        byte[] checkSumBytes = ByteUtil.byteMerger("*".getBytes(), new byte[]{(byte) checkSum});
        checkSumBytes = ByteUtil.byteMerger(checkSumBytes, MESSAGE_END_SYMBOL.getBytes());

        bytes = ByteUtil.byteMerger(bytes, addressBytes);
        bytes = ByteUtil.byteMerger(bytes, checkSumBytes);
        return bytes;
    }

    public static void main(String[] args) {
//        System.out.println(unFormat(format("12345678")));

        byte[] bytes = ConvertUtil.hexStr2Bytes("245231303531383030313112050F0032012AE93B");

        // 接收时间
        int year = ByteUtil.subBytes(bytes, 11, 12)[0]  & 0xFF;
        int month = ByteUtil.subBytes(bytes, 12, 13)[0]  & 0xFF;
        int day = ByteUtil.subBytes(bytes, 13, 14)[0]  & 0xFF;
        int hour = ByteUtil.subBytes(bytes, 14, 15)[0]  & 0xFF;
        int minute = ByteUtil.subBytes(bytes, 15, 16)[0]  & 0xFF;
        int second = ByteUtil.subBytes(bytes, 16, 17)[0]  & 0xFF;
        String dateStr = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
        System.out.println(dateStr);

        // 兼容老设备
        if (bytes.length > 22) {
            int myNumber = Util.bytesToInt2(ByteUtil.subBytes(bytes, 17, 21), 0);

            System.out.println("myNumber: " + myNumber);

            String status = Util.byteToBit(ByteUtil.subBytes(bytes, 21, 22)[0]);
            boolean gpsStatus = status.charAt(7) == '1';
            System.out.println("gpsStatus: " + gpsStatus);
            String communication_from = status.charAt(6) == '1' ? "北斗" : "GPRS";
            System.out.println("communication_from: " + communication_from);
        }
    }
}
