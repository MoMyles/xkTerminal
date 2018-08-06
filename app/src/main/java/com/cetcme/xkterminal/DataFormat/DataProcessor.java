package com.cetcme.xkterminal.DataFormat;

import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.MyApplication;

public class DataProcessor {


    /**
     * 发送短信，包括北斗号 和 手机号
     * @param address
     * @param message
     */
    public static void sendMessage(String address, String message) {
        byte[] bytes;
        if (address.length() == 11) {
            bytes = MessageFormat.format(address, message, MessageFormat.MESSAGE_TYPE_CELLPHONE, 0);
        } else {
            String unique = ConvertUtil.rc4ToHex();
            bytes = MessageFormat.format(address, message, MessageFormat.MESSAGE_TYPE_NORMAL, 0, unique);
        }
        MyApplication.getInstance().sendBytes(bytes);
    }

    /**
     * 发送渔货交易数据
     * @param address
     * @param message
     */
    public static void sendTradeInfo(String address, String message) {
        String unique = ConvertUtil.rc4ToHex();
        MyApplication.getInstance().sendBytes(MessageFormat.format(address, message, MessageFormat.MESSAGE_TYPE_TRADE, 0, unique));
    }



}
