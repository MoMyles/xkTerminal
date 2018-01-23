package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;

import java.io.UnsupportedEncodingException;

/**
 * Created by qiuhong on 22/01/2018.
 */

public class SignFormat {

    public static final String MESSAGE_END_SYMBOL = "\r\n";

    public static String[] unFormat(byte[] frameData) {

        try {
            String head = new String(ByteUtil.subBytes(frameData, 0, 3), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String id = ConvertUtil.bcd2Str(ByteUtil.subBytes(frameData, 3, 12));
        String name = ""; //ConvertUtil.asciiToString(ByteUtil.subBytes(frameData, 3, 12));

        try {
            name = new String(ByteUtil.subBytes(frameData, 12, 24), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        name = Util.stringRemoveZero(name);

        return new String[]{id, name};
    }

    public static byte[] format() {
        byte[] frameData = "$R0".getBytes();
        byte[] idBytes = ConvertUtil.str2Bcd("330283198811240134");
        frameData = ByteUtil.byteMerger(frameData, idBytes);
        try {
            frameData = ByteUtil.byteMerger(frameData, "00000000裘鸿".getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        frameData = ByteUtil.byteMerger(frameData, MESSAGE_END_SYMBOL.getBytes());
        return frameData;
    }

    public static void main(String[] args) {

        byte[] frameData = "$R0".getBytes();
        byte[] idBytes = ConvertUtil.str2Bcd("330283198811240134");
        frameData = ByteUtil.byteMerger(frameData, idBytes);
        try {
            frameData = ByteUtil.byteMerger(frameData, "00000000裘鸿".getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(ConvertUtil.bytesToHexString(frameData));

        String[] unFormatStrings = unFormat(frameData);
        String id = unFormatStrings[0];
        String name = unFormatStrings[1];
        System.out.println(id);
        System.out.println(name);

//        byte[] bytes = new byte[] {
//                (byte) 0x30,
//                (byte) 0x30,
//                (byte) 0x30,
//                (byte) 0x30,
//                (byte) 0x30,
//                (byte) 0x30,
//                (byte) 0x30,
//
//        };
    }

}
