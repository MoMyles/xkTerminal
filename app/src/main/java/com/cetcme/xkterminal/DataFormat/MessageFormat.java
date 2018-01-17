package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;

import java.io.UnsupportedEncodingException;

/**
 * Created by qiuhong on 17/01/2018.
 */

public class MessageFormat {

    private static String messageHead = "$04";

    public static final String MESSAGE_END_SYMBOL = "\r\n";

    public static String[] unFormat(byte[] frameData) {

        try {
            String head = new String(ByteUtil.subBytes(frameData, 0, 3), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String targetAddress = ConvertUtil.bcd2Str(ByteUtil.subBytes(frameData, 3, 9));
        byte b = frameData[14];
        int frameCount = Integer.parseInt(Util.byteToBit(b).substring(0, 2), 2);
        int messageLength = Integer.parseInt(Util.byteToBit(b).substring(2, 8), 2);
        String messageContent = null;
        try {
            messageContent = new String(ByteUtil.subBytes(frameData, 15, 15 + messageLength), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return new String[]{targetAddress, messageContent};
    }

    public static byte[] format(String targetAddress, String message) {
        byte[] bytes = messageHead.getBytes();
        String unique = ConvertUtil.rc4ToHex();
        byte[] addressBytes = ByteUtil.byteMerger(ConvertUtil.str2Bcd(targetAddress), ConvertUtil.str2Bcd(unique));
        byte[] lengthBytes = new byte[]{getDataLengthByte(message, 1)};
        byte[] messageBytes = message.getBytes();

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

        byte[] frameData = format(Util.addZeroString("123456", 12), "你好，这是一条短信00000");
        String[] unFormatStrings = unFormat(frameData);
        String targetAddress = unFormatStrings[0];
        String messageContent = unFormatStrings[1];
        System.out.println(targetAddress);
        System.out.println(messageContent);
    }

    private static byte getDataLengthByte (String message, int frameCountInt) {
        String messageLengthBitStr = Util.addZeroString(Integer.toBinaryString(message.getBytes().length), 6);
        String frameCount = Util.addZeroString(Integer.toBinaryString(frameCountInt), 2);
        return Util.BitToByte(frameCount + messageLengthBitStr);
    }

}
