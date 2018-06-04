package com.cetcme.xkterminal.DataFormat;

import android.telephony.PhoneNumberUtils;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.MyClass.Constant;

import java.io.UnsupportedEncodingException;

/**
 * Created by qiuhong on 17/01/2018.
 */

public class MessageFormat {

    public static final String MESSAGE_TYPE_NORMAL              = "00"; // 普通短信
    public static final String MESSAGE_TYPE_RESCUE              = "01"; // 救护短信
    public static final String MESSAGE_TYPE_CELLPHONE           = "02"; // 手机短信
    public static final String MESSAGE_TYPE_INOUT               = "03"; // 进出港申报
    public static final String MESSAGE_TYPE_AIS                 = "04"; // AIS报警
    public static final String MESSAGE_TYPE_SMS_OPEN            = "05"; // 是否开通短信发送功能
    public static final String MESSAGE_TYPE_ALERT_REMIND        = "06"; // 报警提醒页面
    public static final String MESSAGE_TYPE_SHUT_DOWN           = "07"; // 摇毙功能
    public static final String MESSAGE_TYPE_UPDATE_LOCATION     = "08"; // 更新位置信息
    public static final String MESSAGE_TYPE_REPORT_ALARM        = "09"; // 告警信息，语音播报
    public static final String MESSAGE_TYPE_CALL_THE_ROLL       = "10"; // 夜间点名


    private static final String messageHead = "$04";

    private static final String MESSAGE_END_SYMBOL = "\r\n";

    public static String[] unFormat(byte[] frameData) {

        try {
            String head = new String(ByteUtil.subBytes(frameData, 0, 3), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String targetAddress = ConvertUtil.bcd2Str(ByteUtil.subBytes(frameData, 3, 9)); // 3-9 地址 9-14随机
        targetAddress = Util.stringRemoveZero(targetAddress);

        byte b = frameData[14];
        int frameCount = Integer.parseInt(Util.byteToBit(b).substring(0, 2), 2);
        int messageLength = Integer.parseInt(Util.byteToBit(b).substring(2, 8), 2);

        String messageContent = null;
        String typeString = null;

        int groupId = -1;
        try {
            byte[] typeBytes = ByteUtil.subBytes(frameData, 15, 17);
            typeString = new String(typeBytes, "GB2312");
            if (typeString.substring(0, 1).equals("A")) {
                groupId = (int) typeBytes[1];
            }
            messageContent = new String(ByteUtil.subBytes(frameData, 17, 15 + messageLength), "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new String[]{targetAddress, messageContent, typeString, groupId + "", frameCount + ""};
    }

    public static byte[] format(String targetAddress, String message, String type) {
        message = type + message;
        targetAddress = Util.stringAddZero(targetAddress, 12);
        System.out.println(targetAddress);
        byte[] bytes = messageHead.getBytes();
        String unique = ConvertUtil.rc4ToHex();
        byte[] addressBytes = ByteUtil.byteMerger(ConvertUtil.str2Bcd(targetAddress), ConvertUtil.str2Bcd(unique));
        byte[] lengthBytes = new byte[]{getDataLengthByte(message, 0)};
        byte[] messageBytes = new byte[0];
        try {
            message = shortcutMessage(message); // 裁剪内容到限制54字节内
            messageBytes = message.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] toCheckBytes = ByteUtil.byteMerger(addressBytes, lengthBytes);
        toCheckBytes = ByteUtil.byteMerger(toCheckBytes, messageBytes);

        int checkSum = Util.computeCheckSum(toCheckBytes, 0, toCheckBytes.length);
        byte[] checkSumBytes = ByteUtil.byteMerger("*".getBytes(), new byte[]{(byte) checkSum});
        checkSumBytes = ByteUtil.byteMerger(checkSumBytes, MESSAGE_END_SYMBOL.getBytes());

        bytes = ByteUtil.byteMerger(bytes, toCheckBytes);
        bytes = ByteUtil.byteMerger(bytes, checkSumBytes);
        return bytes;
    }
    public static void main(String[] args) {
//        byte[] frameData = new byte[] {
//                (byte) 0x24,
//                (byte) 0x30,
//                (byte) 0x34,
//                (byte) 0x00,
//                (byte) 0x00,
//                (byte) 0x00,
//                (byte) 0x12,
//                (byte) 0x34,
//                (byte) 0x56,
//                (byte) 0xD9,
//                (byte) 0xD9,
//                (byte) 0xC6,
//                (byte) 0x93,
//                (byte) 0x31,
//                (byte) 0x46,
//                (byte) 0xE4,
//                (byte) 0xBD,
//                (byte) 0xA0,
//                (byte) 0xE5,
//                (byte) 0xA5,
//                (byte) 0xBD,
//                (byte) 0x2A,
//                (byte) 0xA6,
//                (byte) 0x0D,
//                (byte) 0x0A
//            };

//        byte[] frameData = format(Util.stringAddZero("123456", 12), "你好，这是一条短信00000");
//        String[] unFormatStrings = unFormat(frameData);
//        String targetAddress = unFormatStrings[0];
//        String messageContent = unFormatStrings[1];
//        System.out.println(targetAddress);
//        System.out.println(messageContent);
//        System.out.println(ConvertUtil.bytesToHexString("$04".getBytes()));
//        System.out.println(Util.bytesGetHead("$R1".getBytes(),3));

//        try {
//            System.out.println(ConvertUtil.bytesToHexString("一条短信1".getBytes("GB2312")));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

//        System.out.println(ConvertUtil.bytesToHexString("01".getBytes()));

//        byte[] bytes = ConvertUtil.hexStringToByte("24303400000012345607D08F9A8F0C3030C7D7C6DDC8A5C8A5C8A52A1F0D0A");
//        String[] msg = unFormat(bytes);
//        System.out.println("targetAddress: " + msg[0]);
//        System.out.println("messageContent: " + msg[1]);
//        System.out.println("type: " + msg[2]);

        /*
        String content = "121.2a14,31.1234";
        String[] strings = content.split(",");
        try {
            int lon = (int) (Float.parseFloat(strings[0]) * 10000000);
            int lat = (int) (Float.parseFloat(strings[1]) * 10000000);
            System.out.println(lon);
            System.out.println(lat);
        } catch (NumberFormatException e) {
            e.getStackTrace();
        }
        */

        System.out.println(shortcutMessage("一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十一12二"));
    }

    private static byte getDataLengthByte (String message, int frameCountInt) {
        String messageLengthBitStr = null;
        try {
            messageLengthBitStr = Util.stringAddZero(Integer.toBinaryString(message.getBytes("GB2312").length), 6);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("messageLengthBitStr: " + messageLengthBitStr);
        String frameCount = Util.stringAddZero(Integer.toBinaryString(frameCountInt), 2);
        System.out.println("frameCount: " + frameCount);
        return Util.BitToByte(frameCount + messageLengthBitStr);
    }

    public static String shortcutMessage(String message) {
        try {
            if (message.getBytes("GB2312").length > 54) {
                return shortcutMessage(message.substring(0, message.length() - 1));
            } else {
                return message;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
