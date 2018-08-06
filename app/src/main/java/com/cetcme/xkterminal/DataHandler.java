package com.cetcme.xkterminal;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.IDFormat;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.SignFormat;
import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.ScreenBrightness;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Socket.SocketServer;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Proxy.GroupProxy;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DataHandler extends Handler {

    public static final int SERIAL_PORT_RECEIVE_NEW_MESSAGE = 0x01; // 收到信短信
    public static final int SERIAL_PORT_MESSAGE_SEND_SUCCESS = 0x02; // 发送短信成功
    public static final int SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM = 0x03; // 时间、通信来源
    public static final int SERIAL_PORT_TIME = 0x13; // 时间

    public static final int SERIAL_PORT_ALERT_SEND_SUCCESS = 0x04; // 报警成功
    public static final int SERIAL_PORT_SHOW_ALERT_ACTIVITY = 0x05; // 显示报警类型选择框
    public static final int SERIAL_PORT_RECEIVE_NEW_SIGN = 0x06;  // 收到新打卡
    public static final int SERIAL_PORT_RECEIVE_NEW_ALERT = 0x07; // 收到新报警
    public static final int SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS = 0x08;  // 屏幕亮度
    public static final int SERIAL_PORT_SHUT_DOWN = 0x09; // 关机
    public static final int SERIAL_PORT_ALERT_START = 0x10; // 开始报警
    public static final int SERIAL_PORT_ALERT_FAIL = 0x11; // 报警失败
    public static final int SERIAL_PORT_ID_EDIT_OK = 0x12; // 终端ID编辑成功

    public static final int SERIAL_PORT_CHECK = 0x14;

    private MyApplication myApplication;

    private DbManager db;
    private Timer timer30 = null;

    public DataHandler(MyApplication myApplication) {
        this.myApplication = myApplication;
        this.db = MyApplication.getInstance().getDb();
        timer30 = new Timer();
        timer30.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!MyApplication.getInstance().isLocated) {
                    MyApplication.getInstance().mainActivity.sendBootData();
                }
            }
        }, Constant.SEND_BOOT_DATA_TIME);
    }

    String content = "";

    @Override
    public void handleMessage(Message msg) {
        try {
            byte[] bytes = msg.getData().getByteArray("bytes");

            switch (msg.what) {//根据收到的消息的what类型处理
                case SERIAL_PORT_RECEIVE_NEW_MESSAGE:
                    // 如果卫星中断 则返回 不显示短信
                    if (!MyApplication.isLocated) return;
                    // 收到新短信
                    String[] messageStrings = MessageFormat.unFormat(bytes);
                    String address = messageStrings[0];
                    String content = messageStrings[1];
                    String type = messageStrings[2];
                    int group = Integer.parseInt(messageStrings[3]);
                    int frameCount = Integer.parseInt(messageStrings[4]);
                    if (!TextUtils.isEmpty(content) && content.startsWith("$06")){
                        //建林定位信息
                        String lat = content.substring(3, 13);
                        String latDirect = content.substring(13, 14);
                        String lon = content.substring(14, 25);
                        String lonDirect = content.substring(25, 26);
                        String speed = content.substring(26, 31);
                        String cog = content.substring(31, 36);
                        String voltage = content.substring(36, 37);
                        LocationBean lb = null;
                        if (MyApplication.getInstance().getCurrentLocation() != null) {
                            lb = MyApplication.getInstance().getCurrentLocation();
                        } else {
                            lb = new LocationBean();
                        }
                        if (lb != null) {
                            try {
                                if ("N".equals(latDirect)) {
                                    lb.setLatitude((int) (Double.parseDouble(lat) * 1e5));
                                } else {
                                    lb.setLatitude((int) (-1 * Double.parseDouble(lat) * 1e5));
                                }
                                if ("E".equals(lonDirect)) {
                                    lb.setLongitude((int) (Double.parseDouble(lon) * 1e5));
                                } else {
                                    lb.setLongitude((int) (-1 * Double.parseDouble(lon) * 1e5));
                                }
                                lb.setSpeed(Float.parseFloat(speed));
                                lb.setHeading(Float.parseFloat(cog));
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        MyApplication.voltage = voltage == null ? "-" : voltage;
                        break;
                    }
                    switch (type) {
                        // 普通短信
                        case MessageFormat.MESSAGE_TYPE_NORMAL:
                            this.content += content;
                            if (frameCount == 0) {
                                SoundPlay.playMessageSound(myApplication.mainActivity);
                                myApplication.mainActivity.addMessage(address, this.content, false);
                                myApplication.mainActivity.modifyGpsBarMessageCount();
                                Toast.makeText(myApplication.mainActivity, "您有新的短信", Toast.LENGTH_SHORT).show();
                                this.content = "";
                            }
                            break;
                        // 救护短信
                        case MessageFormat.MESSAGE_TYPE_RESCUE:
                            SoundPlay.playMessageSound(myApplication.mainActivity);
                            myApplication.sendLightOn(true);
                            myApplication.mainActivity.showMessageDialog(address, content, MessageDialogActivity.TYPE_RESCUE);
                            break;
                        // 开启关闭短信功能
                        case MessageFormat.MESSAGE_TYPE_SMS_OPEN:
                            PreferencesUtils.putBoolean(myApplication.mainActivity, "canSendSms", content.equals("1"));
                            break;
                        // 报警提醒
                        case MessageFormat.MESSAGE_TYPE_ALERT_REMIND:
                            SoundPlay.playMessageSound(myApplication.mainActivity);
                            myApplication.mainActivity.showMessageDialog(address, content, MessageDialogActivity.TYPE_ALERT);
                            break;
                        // 摇毙功能
                        case MessageFormat.MESSAGE_TYPE_SHUT_DOWN:
                            PreferencesUtils.putBoolean(myApplication.mainActivity, "shutdown", true);
                            System.exit(0);
                            break;
                        // 更新位置
                        case MessageFormat.MESSAGE_TYPE_UPDATE_LOCATION:
                            String[] strings = content.split(",");
                            if (strings.length == 5) {
                                try {
                                    int lon = (int) (Float.parseFloat(strings[0]) * 10000000);
                                    int lat = (int) (Float.parseFloat(strings[1]) * 10000000);
                                    float speed = Float.parseFloat(strings[2]);
                                    float head = Float.parseFloat(strings[3]);
                                    MyApplication.voltage = strings[4];
                                    myApplication.getCurrentLocation().setLongitude(lon);
                                    myApplication.getCurrentLocation().setLatitude(lat);
                                    myApplication.getCurrentLocation().setSpeed(speed);
                                    myApplication.getCurrentLocation().setHeading(head);
                                    myApplication.getCurrentLocation().setAcqtime(Constant.SYSTEM_DATE);
                                    EventBus.getDefault().post(myApplication.getCurrentLocation());
                                } catch (NumberFormatException e) {
                                    e.getStackTrace();
                                }
                            }
                            break;
                        // 夜间点名
                        case MessageFormat.MESSAGE_TYPE_CALL_THE_ROLL:
                            SoundPlay.playMessageSound(myApplication.mainActivity);
                            // TODO:
                            myApplication.mainActivity.showMessageDialog(address, content, MessageDialogActivity.TYPE_CALL_ROLL);
                            break;
                        // 告警信息，语音播报
                        case MessageFormat.MESSAGE_TYPE_REPORT_ALARM:
                            myApplication.mainActivity.showMessageDialog(address, content, MessageDialogActivity.TYPE_ALARM);
                            MainActivity.play("告警信息: " + content);
                            break;
                        // 添加删除组播号
                        case MessageFormat.MESSAGE_TYPE_GROUP: {
                            String[] value = content.split(",");
                            if (value[0].equals("0")) {
                                // 删除
                                myApplication.mainActivity.addGroup("group", value[1]);
                                Toast.makeText(myApplication.mainActivity, "您已加入分组：" + value[1], Toast.LENGTH_SHORT).show();
                            } else {
                                // 添加
                                myApplication.mainActivity.deleteGroup(value[1]);
                                Toast.makeText(myApplication.mainActivity, "您已退出分组：" + value[1], Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        // 自检OK和海图序列号
                        case MessageFormat.MESSAGE_TYPE_CHECK_AND_MAP: {
                            if (!content.equals("OK")) {
                                if (!PreferencesUtils.getBoolean(MyApplication.getInstance().getApplicationContext(), "self_check_result", false)) {
                                    // 注册海图
                                    PreferencesUtils.putString(MyApplication.getInstance().mainActivity, "yimaSerial", content);
                                    // 设置完成后提示 重新进入app
                                    new QMUIDialog.MessageDialogBuilder(MyApplication.getInstance().mainActivity)
                                            .setTitle("提示")
                                            .setMessage("海图序列号设置成功，请重新打开app")
                                            .addAction("确定", new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    System.exit(0);
                                                }
                                            })
                                            .show();
                                }
                            }
                            // 自检成功
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("apiType", "self_check_2");
                            EventBus.getDefault().post(new SmsEvent(jsonObject));
                            break;
                        }
                        // 渔货交易数据
                        case MessageFormat.MESSAGE_TYPE_TRADE: {
                            // TODO：平台号
                            final String unique = ConvertUtil.rc4ToHex();
                            MyApplication.getInstance().sendBytes(MessageFormat.format("", content, MessageFormat.MESSAGE_TYPE_TRADE, 0, unique));
                            break;
                        }
                        default:
                            // 判断分组 group -1为非分组短信，其他为组号，
                            if (group == -1 || GroupProxy.hasGroup(db, group)) {
                                SoundPlay.playMessageSound(myApplication.mainActivity);
                                myApplication.mainActivity.addMessage(address, content, false);
                                myApplication.mainActivity.modifyGpsBarMessageCount();
                                Toast.makeText(myApplication.mainActivity, "您有新的短信", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                    break;
                case SERIAL_PORT_MESSAGE_SEND_SUCCESS:

                    if (!MyApplication.isLocated) return;

                    // 短信发送成功
                    Toast.makeText(myApplication.mainActivity, "短信发送成功", Toast.LENGTH_SHORT).show();

                    String lastSendTimeSave = PreferencesUtils.getString(myApplication.mainActivity, "lastSendTimeSave");
                    PreferencesUtils.putString(myApplication.mainActivity, "lastSendTime", lastSendTimeSave);

                    // 用于去掉2秒后显示发送失败提示
                    myApplication.mainActivity.messageSendFailed = false;
                    myApplication.messageSendFailed = false;

                    // 返回成功socket
                    JSONObject sendJson = new JSONObject();
                    try {
                        sendJson.put("apiType", "sms_send");
                        sendJson.put("code", 0);
                        sendJson.put("msg", "发送成功");

                        SocketServer.send(sendJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM:
                    // 先处理后面部分，时间部分由下一个case处理，不加break
                    int myNumber = Util.bytesToInt2(ByteUtil.subBytes(bytes, 17, 21), 0);
                    if (myNumber != 0) {
                        PreferencesUtils.putString(myApplication.mainActivity, "myNumber", myNumber + "");
                        MainActivity.myNumber = myNumber + "";
                        System.out.println("myNumber: " + myNumber);
                    }

                    String status = Util.byteToBit(ByteUtil.subBytes(bytes, 21, 22)[0]);

                    // 重新启用 来自数据串口的是否定位信息
                    // 开机显示自检中loading 1 toast自检完成 如果0 弹窗 dismiss loading
                    // 运行过程中收到 则不显示自检完成
                    if (myApplication.mainActivity != null) {
                        final boolean gpsStatus = status.charAt(7) == '1';

                        if (!gpsStatus) {
                            myApplication.mainActivity.showMessageDialog("自身设备", "卫星中断故障", MessageDialogActivity.TYPE_ALARM);
                            MainActivity.play("卫星中断故障");
                            myApplication.mainActivity.dismissSelfCheckHud();
                        } else {
                            if (timer30 != null) {
                                timer30.cancel();
                            }
                            if (myApplication.mainActivity.isSelfCheckLoading) {
                                Toast.makeText(myApplication.mainActivity, "自检完成", Toast.LENGTH_SHORT).show();
                                MainActivity.play("自检完成");
                                myApplication.mainActivity.dismissSelfCheckHud();
                            }
                        }
                        myApplication.isLocated = gpsStatus;
                        myApplication.mainActivity.gpsBar.setGPSStatus(gpsStatus);

//                        if (myApplication.mainActivity.isSelfCheckLoading) {
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    myApplication.isLocated = gpsStatus;
//                                    myApplication.mainActivity.gpsBar.setGPSStatus(gpsStatus);
//                                    myApplication.mainActivity.dismissSelfCheckHud();
//                                    if (gpsStatus) {
//
//                                    }
//                                }
//                            }, 2000);
//                        }

                        String communication_from = status.charAt(6) == '1' ? "北斗" : "GPRS";
                        PreferencesUtils.putString(myApplication.mainActivity, "communication_from", communication_from);
                    }
                    // 这里不加break
                case SERIAL_PORT_TIME: {
                    // 接收时间
                    int year = ByteUtil.subBytes(bytes, 11, 12)[0] & 0xFF;
                    int month = ByteUtil.subBytes(bytes, 12, 13)[0] & 0xFF;
                    int day = ByteUtil.subBytes(bytes, 13, 14)[0] & 0xFF;
                    int hour = ByteUtil.subBytes(bytes, 14, 15)[0] & 0xFF;
                    int minute = ByteUtil.subBytes(bytes, 15, 16)[0] & 0xFF;
                    int second = ByteUtil.subBytes(bytes, 16, 17)[0] & 0xFF;
                    String dateStr = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
                    Date date = DateUtil.parseStringToDate(dateStr);
                    // 加8小时

                    int originalTimeZone = PreferencesUtils.getInt(myApplication.mainActivity, "time_zone");
                    if (originalTimeZone == -1) originalTimeZone = Constant.TIME_ZONE;

                    long rightTime = date.getTime() + (originalTimeZone - 12) * 3600 * 1000;
                    Date rightDate = new Date(rightTime);

                    SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
                    if (Integer.parseInt(yearSdf.format(rightDate)) >= 2018) {
                        System.out.println("设置系统时间");
                        Constant.SYSTEM_DATE = rightDate;
                    }
                    System.out.println(rightDate);
                    break;
                }
                case SERIAL_PORT_ID_EDIT_OK:
                    // $R2 刷卡器id修改成功 获取 获取成功
                    String deviceID = IDFormat.unFormat(bytes);
                    System.out.println("deviceID: " + deviceID);

                    JSONObject sendJSON = new JSONObject();
                    try {
                        sendJSON.put("apiType", "device_id");
                        sendJSON.put("code", "0");
                        sendJSON.put("msg", "获取成功");
                        sendJSON.put("deviceID", deviceID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SocketServer.send(sendJSON);
                    EventBus.getDefault().post(new SmsEvent(sendJSON));
//                    Toast.makeText(myApplication.mainActivity, "终端ID：" + deviceID, Toast.LENGTH_LONG).show();
                    break;
                case SERIAL_PORT_ALERT_SEND_SUCCESS:
                    if (!MyApplication.isLocated) return;
                    // 报警发送成功
                    myApplication.mainActivity.gpsBar.showAlerting(false);
                    StringBuffer str = new StringBuffer("遇险报警");
                    if (MyApplication.getInstance().isAisConnected) {
                        str.append("和AIS安全广播");
                    }
                    str.append("发送成功");
                    Toast.makeText(myApplication.mainActivity, str.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case SERIAL_PORT_SHOW_ALERT_ACTIVITY:
                    if (!MyApplication.isLocated) return;
                    // 显示报警activity
                    myApplication.mainActivity.gpsBar.showAlerting(false);
                    myApplication.mainActivity.showDangerDialog();
                    break;
                case SERIAL_PORT_RECEIVE_NEW_ALERT:
                    if (!MyApplication.isLocated) return;
                    // 增加报警记录，显示收到报警
                    myApplication.mainActivity.gpsBar.showAlerting(false);
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("apiType", "showAlertInHomePage");
                        EventBus.getDefault().post(new SmsEvent(jsonObject));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    PreferencesUtils.putBoolean(myApplication.mainActivity, "homePageAlertView", true);
                    SoundPlay.startAlertSound(myApplication.mainActivity);

                    byte[] alertBytes = ByteUtil.subBytes(bytes, 11, 13);
                    if (alertBytes[0] == 0x02 && alertBytes[1] == 0x00) {
                        // 落水报警
                        Toast.makeText(myApplication.mainActivity, "收到落水报警", Toast.LENGTH_SHORT).show();
                        myApplication.mainActivity.addAlertLog("落水");
                    } else if (alertBytes[0] == 0x10 && alertBytes[1] == 0x00) {
                        // 解除报警
                        PreferencesUtils.putBoolean(myApplication.mainActivity, "homePageAlertView", false);
                        if (myApplication.mainActivity.mainFragment != null) {
                            myApplication.mainActivity.mainFragment.showMainLayout();
                        }
                        myApplication.mainActivity.gpsBar.cancelAlert();
                        SoundPlay.stopAlertSound();
                    }

                    break;
                case SERIAL_PORT_RECEIVE_NEW_SIGN: {
                    // 接收身份证信息
                    String[] idStrings = SignFormat.unFormat(bytes);
                    String id = idStrings[0];
                    String name = idStrings[1];
                    String nation = "---";
                    String idAddress = "---";

                    String timeBytesStr = idStrings[2];
                    byte[] timeBytes = ConvertUtil.hexStringToByte(timeBytesStr);
                    int year = Integer.valueOf(ByteUtil.bcd2Str(ByteUtil.subBytes(timeBytes, 0, 1)));
                    int month = Integer.valueOf(ByteUtil.bcd2Str(ByteUtil.subBytes(timeBytes, 1, 2)));
                    int day = Integer.valueOf(ByteUtil.bcd2Str(ByteUtil.subBytes(timeBytes, 2, 3)));
                    int hour = Integer.valueOf(ByteUtil.bcd2Str(ByteUtil.subBytes(timeBytes, 3, 4)));
                    int minute = Integer.valueOf(ByteUtil.bcd2Str(ByteUtil.subBytes(timeBytes, 4, 5)));
                    int second = Integer.valueOf(ByteUtil.bcd2Str(ByteUtil.subBytes(timeBytes, 5, 6)));

                    Date date;
                    SimpleDateFormat sdf = new SimpleDateFormat("yy");
                    if (year < Integer.valueOf(sdf.format(Constant.SYSTEM_DATE))) {
                        date = Constant.SYSTEM_DATE;
                    } else {
                        String dateStr = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
                        date = DateUtil.parseStringToDate(dateStr);
                        // 加8小时

                        int originalTimeZone = PreferencesUtils.getInt(myApplication.mainActivity, "time_zone");
                        if (originalTimeZone == -1) originalTimeZone = Constant.TIME_ZONE;

                        long rightTime = date.getTime() + (originalTimeZone - 12) * 3600 * 1000;
                        date = new Date(rightTime);
                    }
                    myApplication.mainActivity.showIDCardDialog(id, name, nation, idAddress, date);
                    SoundPlay.playSignSound(myApplication);
                    break;
                }
                case SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS:
                    // 调节背光
                    ScreenBrightness.modifyBrightness(myApplication.mainActivity);
                    break;
                case SERIAL_PORT_SHUT_DOWN:
                    // 显示关机hud
                    myApplication.mainActivity.showShutDownHud();

                    // 发送关机包
                    byte[] sendBytes = "$07".getBytes();
                    byte[] contentBytes = "OK".getBytes();
                    int checkSum = Util.computeCheckSum(contentBytes, 0, contentBytes.length);
                    byte[] checkSumBytes = ByteUtil.byteMerger("*".getBytes(), new byte[]{(byte) checkSum});
                    checkSumBytes = ByteUtil.byteMerger(checkSumBytes, "\r\n".getBytes());
                    sendBytes = ByteUtil.byteMerger(sendBytes, contentBytes);
                    sendBytes = ByteUtil.byteMerger(sendBytes, checkSumBytes);
                    myApplication.sendBytes(sendBytes);
                    break;
                case SERIAL_PORT_ALERT_START:
                    myApplication.mainActivity.gpsBar.showAlerting(true);
                    break;
                case SERIAL_PORT_ALERT_FAIL:
                    myApplication.mainActivity.gpsBar.showAlerting(false);
                    myApplication.mainActivity.showAlertFailDialog();
                    break;
                case SERIAL_PORT_CHECK: {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "self_check_1");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                    break;
                }
                default:
                    super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
