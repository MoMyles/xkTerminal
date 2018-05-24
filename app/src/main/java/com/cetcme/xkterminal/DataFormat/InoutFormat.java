package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.MyClass.DateUtil;

import java.util.Date;

import static com.cetcme.xkterminal.MyClass.GPSFormatUtils.formatGps;

public class InoutFormat {

    public static final String MESSAGE_TYPE_INOUT = "03";
    private static final String messageHead = "$04";

    private static final String MESSAGE_END_SYMBOL = "\r\n";

    public static byte[] format(String targetAddress, int type, int count, int lon, int lat, Date time) {

        targetAddress = Util.stringAddZero(targetAddress, 12);

        byte[] bytes = messageHead.getBytes();
        String unique = ConvertUtil.rc4ToHex();
        byte[] addressBytes = ByteUtil.byteMerger(ConvertUtil.str2Bcd(targetAddress), ConvertUtil.str2Bcd(unique));

        byte[] typeBytes = type == 1 ? new byte[]{0x01} : new byte[]{0x02};
        byte[] countBytes = new byte[] {(byte) count};
        byte[] lonBytes = formatGps((double) lon / 10000000);
        byte[] latBytes = formatGps((double) lat / 10000000);
        String dateStr = DateUtil.Date2String(time, "yyMMddHHmmss");
        byte[] timeBytes = ConvertUtil.str2Bcd(dateStr);

        byte[] messageBytes;
        messageBytes = ByteUtil.byteMerger(MESSAGE_TYPE_INOUT.getBytes(), typeBytes);
        messageBytes = ByteUtil.byteMerger(messageBytes, countBytes);
        messageBytes = ByteUtil.byteMerger(messageBytes, lonBytes);
        messageBytes = ByteUtil.byteMerger(messageBytes, latBytes);
        messageBytes = ByteUtil.byteMerger(messageBytes, timeBytes);

        int messageLength = messageBytes.length;
        byte[] lengthBytes = new byte[]{(byte) messageLength};

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

    }
}
