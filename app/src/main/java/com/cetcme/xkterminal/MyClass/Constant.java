package com.cetcme.xkterminal.MyClass;

/**
 * Created by qiuhong on 11/01/2018.
 */

public class Constant {

    // 短信内容最大长度，0为不限
    public static int MESSAGE_CONTENT_MAX_LENGTH = 56;

    // 打卡对话框自动关闭时间，0为不关闭，ms
    public static int IDCARD_REMAIN_TIME = 5000;

    // 报警对话框自动关闭时间，0为不关闭，ms
    public static int ALERT_REMAIN_TIME = 0;

    // 未定位闪烁间隔，0为不闪烁，ms
    public static int NO_GPS_FLASH_TIME = 700;

    // 短信发送间隔
    public static int MESSAGE_SEND_LIMIT_TIME = 60000;

    // 紧急报警闪烁间隔，0为不闪烁，ms
    public static int ALERT_FLASH_TIME = 500;

}
