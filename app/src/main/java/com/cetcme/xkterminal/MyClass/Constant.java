package com.cetcme.xkterminal.MyClass;

import java.util.Date;

/**
 * Created by qiuhong on 11/01/2018.
 */

public class Constant {

    public static final Boolean PHONE_TEST = false;

    // 服务器北斗地址
    public static final String SERVER_BD_NUMBER = "372741";

    // socket 服务器端口
    public static final int SOCKET_SERVER_PORT = 9999;

    // socket 文件服务器端口
    public static final int FILE_SOCKET_SERVER_PORT = 9998;

    // 时间
    public static Date SYSTEM_DATE = new Date();

    // 时区
    public static int TIME_ZONE = 20; // +8   0 ~ 24

    // 短信内容最大长度，0为不限
    public static final int MESSAGE_CONTENT_MAX_LENGTH = 64; // 54; TODO: fake

    // 短信发送失败等待时间
    public static final int MESSAGE_FAIL_TIME = 7000;

    // 打卡对话框自动关闭时间，0为不关闭，ms
    public static final int IDCARD_REMAIN_TIME = 5000;

    // 报警对话框自动关闭时间，0为不关闭，ms
    public static final int ALERT_REMAIN_TIME = 0;

    // 未定位闪烁间隔，0为不闪烁，ms
    public static final int NO_GPS_FLASH_TIME = 700;
    public static final int NO_AIS_FLASH_TIME = 700;

    // 短信发送间隔
    public static final int MESSAGE_SEND_LIMIT_TIME = 60000;

    // 开机自检超时时间
    public static final int SELF_CHECK_TIME_OUT = 30 * 1000; //TODO

    // 开机之后隔多久发开机数据包
    public static final int SEND_BOOT_DATA_TIME = 20 * 1000;

    // 紧急报警闪烁间隔，0为不闪烁，ms
    public static final int ALERT_FLASH_TIME = 500;

    // 数据串口码率
    public static final int SERIAL_DATA_PORT_BAUD_RATE = 9600;
    // gps串口码率
//    public static final int SERIAL_GPS_PORT_BAUD_RATE = 9600;
    public static final int SERIAL_AIS_PORT_BAUD_RATE = 38400;

    // GPS串口路径
//    public static final String SERIAL_GPS_PORT_PATH = "/dev/ttyS1";
    public static final String SERIAL_AIS_PORT_PATH = "/dev/ttyS1";

    // 数据串口路径
    public static final String SERIAL_DATA_PORT_PATH = "/dev/ttyS3";

    // 显示序号
    public static final boolean SHOW_NUMBER_MESSAGE_LIST = true;
    public static final boolean SHOW_NUMBER_MSG_TEMP_LIST = true;
    public static final boolean SHOW_NUMBER_FRIEND_LIST = true;

    // 存数据库内容的条数限制 TODO 确定数量
    public static final int LIMIT_MESSAGE = 2000; //?
    public static final int LIMIT_INOUT = 50; //?
    public static final int LIMIT_FRIEND = 50;
    public static final int LIMIT_SMS_TEMP = 50;
    public static final int LIMIT_PIN = 50; //?
}
