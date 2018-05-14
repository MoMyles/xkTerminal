package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;

import java.io.UnsupportedEncodingException;

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
        System.out.println(unFormat(format("12345678")));
    }
}
