package com.cetcme.xkterminal.MyClass;

/**
 * Created by qiuhong on 11/01/2018.
 */

public class Constant {

    // 短信内容最大长度，0为不限
    public static int MESSAGE_CONTENT_MAX_LENGTH = 28;

    // 打卡对话框自动关闭时间，0为不关闭，ms
    public static int IDCARD_REMAIN_TIME = 5000;

    // 报警对话框自动关闭时间，0为不关闭，ms
    public static int ALERT_REMAIN_TIME = 5000;

    // 未定位闪烁间隔，0为不闪烁，ms
    public static int NO_GPS_FLASH_TIME = 700;

    public static int MSG_WHAT_$04 = 0x01;
    public static int MSG_WHAT_$R4 = 0x02;
    public static int MSG_WHAT_$R1 = 0x03;
    public static int MSG_WHAT_$R5 = 0x04;
    public static int MSG_WHAT_$R0 = 0x05;
}
