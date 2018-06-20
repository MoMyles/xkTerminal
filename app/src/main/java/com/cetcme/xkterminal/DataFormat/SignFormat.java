package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.MyClass.Constant;

import java.io.UnsupportedEncodingException;
import java.util.Date;

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

        try {
            String targetAddress = new String(ByteUtil.subBytes(frameData, 3, 11), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] randem = ByteUtil.subBytes(frameData, 11, 12);
        String id = ConvertUtil.turnIdbytesToString(frameData, 12, 9); // from 12 t0 21
        String name = ""; //ConvertUtil.asciiToString(ByteUtil.subBytes(frameData, 3, 12));

        byte[] timeBytes = ByteUtil.subBytes(frameData, 21, 27);

        try {
            name = ConvertUtil.turnNameBytesToString(ByteUtil.subBytes(frameData, 27, 39), 0);
//            name = new String(ByteUtil.subBytes(frameData, 20, 32), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        name = Util.stringRemoveZero(name);

        return new String[]{id, name, ConvertUtil.bytesToHexString(timeBytes)};
    }

    public static byte[] format() {
        byte[] frameData = "$R0".getBytes();
        byte[] idBytes = ConvertUtil.str2Bcd("330283198811240134");
        frameData = ByteUtil.byteMerger(frameData, "12345678".getBytes());
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

//        byte[] frameData = "$R0".getBytes();
//        byte[] idBytes = ConvertUtil.str2Bcd("330283198811240134");
//        frameData = ByteUtil.byteMerger(frameData, idBytes);
//        try {
//            frameData = ByteUtil.byteMerger(frameData, "00000000裘鸿".getBytes("GBK"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        System.out.println(ConvertUtil.bytesToHexString(frameData));
//
//        String[] unFormatStrings = unFormat(frameData);
//        String id = unFormatStrings[0];
//        String name = unFormatStrings[1];
//        System.out.println(id);
//        System.out.println(name);

//        byte[] bytes = new byte[] {
//                (byte) 0xD8,
//                (byte) 0x88,
//                (byte) 0x3F,
//                (byte) 0x9E,
//                (byte) 0x20,
//                (byte) 0x00,
//                (byte) 0x20,
//                (byte) 0x00,
//                (byte) 0x20,
//                (byte) 0x00,
//                (byte) 0x20,
//                (byte) 0x00
//        };
//
//        try {
//            String str = ConvertUtil.turnNameBytesToString(bytes,0);
//            System.out.println(str);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        byte[] bytes = "$01".getBytes();
        bytes = ByteUtil.byteMerger(bytes, new byte[] {0x01, 0x00});
        bytes = ByteUtil.byteMerger(bytes, "hh".getBytes());
        bytes = ByteUtil.byteMerger(bytes, "\r\n".getBytes());

        System.out.println(ConvertUtil.bytesToHexString(bytes));

    }

}
