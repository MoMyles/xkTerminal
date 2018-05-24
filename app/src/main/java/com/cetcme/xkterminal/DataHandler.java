package com.cetcme.xkterminal;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.IDFormat;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.SignFormat;
import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.ScreenBrightness;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Socket.SocketServer;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataHandler extends Handler {

    public static final int SERIAL_PORT_RECEIVE_NEW_MESSAGE = 0x01;
    public static final int SERIAL_PORT_MESSAGE_SEND_SUCCESS = 0x02;
    public static final int SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM = 0x03;
    public static final int SERIAL_PORT_TIME = 0x13;

    public static final int SERIAL_PORT_ALERT_SEND_SUCCESS = 0x04;
    public static final int SERIAL_PORT_SHOW_ALERT_ACTIVITY = 0x05;
    public static final int SERIAL_PORT_RECEIVE_NEW_SIGN = 0x06;
    public static final int SERIAL_PORT_RECEIVE_NEW_ALERT = 0x07;
    public static final int SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS = 0x08;
    public static final int SERIAL_PORT_SHUT_DOWN = 0x09;
    public static final int SERIAL_PORT_ALERT_START = 0x10;
    public static final int SERIAL_PORT_ALERT_FAIL = 0x11;
    public static final int SERIAL_PORT_ID_EDIT_OK = 0x12;

    private MainActivity mainActivity;
    private MyApplication myApplication;

    public DataHandler(MainActivity mainActivity, MyApplication myApplication) {
        this.myApplication = myApplication;
        this.mainActivity = mainActivity;
    }

    @Override
    public void handleMessage(Message msg) {
        byte[] bytes = msg.getData().getByteArray("bytes");

        switch (msg.what) {//根据收到的消息的what类型处理
            case SERIAL_PORT_RECEIVE_NEW_MESSAGE:
                // 收到新短信
                String[] messageStrings = MessageFormat.unFormat(bytes);
                String address = messageStrings[0];
                String content = messageStrings[1];
                String type    = messageStrings[2];
                int group      = Integer.parseInt(messageStrings[3]);

                // 判断类型 普通短信 还是 救护短信
                if (type.equals(MessageFormat.MESSAGE_TYPE_RESCUE)) {
                    myApplication.sendLightOn(true);
                    mainActivity.showRescueDialog(content);
                    mainActivity.addMessage(address, content, true);
                } else {

                    // 判断分组 group -1为非分组短信，其他为组号，
                    int ownGroup = PreferencesUtils.getInt(mainActivity, "group");
                    if (group == -1 || group == ownGroup) { // 判断是分组短信
                        SoundPlay.playMessageSound(mainActivity);
                        mainActivity.addMessage(address, content, false);
                        mainActivity.modifyGpsBarMessageCount();
                        Toast.makeText(mainActivity, "您有新的短信", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case SERIAL_PORT_MESSAGE_SEND_SUCCESS:
                // 短信发送成功
                Toast.makeText(mainActivity, "短信发送成功", Toast.LENGTH_SHORT).show();

                String lastSendTimeSave = PreferencesUtils.getString(mainActivity, "lastSendTimeSave");
                PreferencesUtils.putString(mainActivity, "lastSendTime", lastSendTimeSave);

                // 用于去掉2秒后显示发送失败提示
                mainActivity.messageSendFailed = false;
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
                PreferencesUtils.putString(mainActivity, "myNumber", myNumber + "");
                MainActivity.myNumber = myNumber + "";
                System.out.println("myNumber: " + myNumber);

                String status = Util.byteToBit(ByteUtil.subBytes(bytes, 21, 22)[0]);
                boolean gpsStatus = status.charAt(7) == '1';
                mainActivity.gpsBar.setGPSStatus(gpsStatus);
                String communication_from = status.charAt(6) == '1' ? "北斗" : "GPRS";
                PreferencesUtils.putString(mainActivity, "communication_from", communication_from);
                // 这里不加break
            case SERIAL_PORT_TIME:
                // 接收时间
                int year = ByteUtil.subBytes(bytes, 11, 12)[0]  & 0xFF;
                int month = ByteUtil.subBytes(bytes, 12, 13)[0]  & 0xFF;
                int day = ByteUtil.subBytes(bytes, 13, 14)[0]  & 0xFF;
                int hour = ByteUtil.subBytes(bytes, 14, 15)[0]  & 0xFF;
                int minute = ByteUtil.subBytes(bytes, 15, 16)[0]  & 0xFF;
                int second = ByteUtil.subBytes(bytes, 16, 17)[0]  & 0xFF;
                String dateStr = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
                Date date = DateUtil.parseStringToDate(dateStr);
                // 加8小时

                int originalTimeZone = PreferencesUtils.getInt(mainActivity, "time_zone");
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
                Toast.makeText(mainActivity, "终端ID：" + deviceID, Toast.LENGTH_LONG).show();

//                    new QMUIDialog.MessageDialogBuilder(mainActivity)
//                            .setTitle("终端ID")
//                            .setMessage(deviceID)
//                            .addAction("确定", new QMUIDialogAction.ActionListener() {
//                                @Override
//                                public void onClick(QMUIDialog dialog, int index) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
                break;
            case SERIAL_PORT_ALERT_SEND_SUCCESS:
                // 报警发送成功
                mainActivity.gpsBar.showAlerting(false);
                Toast.makeText(mainActivity, "遇险报警发送成功", Toast.LENGTH_SHORT).show();
                break;
            case SERIAL_PORT_SHOW_ALERT_ACTIVITY:
                // 显示报警activity
                mainActivity.gpsBar.showAlerting(false);
                mainActivity.showDangerDialog();
                break;
            case SERIAL_PORT_RECEIVE_NEW_ALERT:
                // 增加报警记录，显示收到报警
                mainActivity.gpsBar.showAlerting(false);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "showAlertInHomePage");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PreferencesUtils.putBoolean(mainActivity, "homePageAlertView", true);
                SoundPlay.startAlertSound(mainActivity);

                byte[] alertBytes = ByteUtil.subBytes(bytes, 11, 13);
                if (alertBytes[0] == 0x02 && alertBytes[1] == 0x00) {
                    // 落水报警
                    Toast.makeText(mainActivity, "收到落水报警", Toast.LENGTH_SHORT).show();
                    mainActivity.addAlertLog("落水");
                } else if (alertBytes[0] == 0x10 && alertBytes[1] == 0x00){
                    // 解除报警
                    PreferencesUtils.putBoolean(mainActivity, "homePageAlertView", false);
                    if (mainActivity.mainFragment != null) {
                        mainActivity.mainFragment.showMainLayout();
                    }
                    mainActivity.gpsBar.cancelAlert();
                    SoundPlay.stopAlertSound();
//                        Toast.makeText(getApplicationContext(), "收到遇险报警", Toast.LENGTH_SHORT).show();
//                        mainActivity.addAlertLog("");
                }

                break;
            case SERIAL_PORT_RECEIVE_NEW_SIGN:
                // 接收身份证信息
                String[] idStrings = SignFormat.unFormat(bytes);
                String id = idStrings[0];
                String name = idStrings[1];
                String nation = "--";
                String idAddress = "xx市xx区xx小区xx幢xx室";
                mainActivity.showIDCardDialog(id, name, nation, idAddress);
                break;
            case SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS:
                // 调节背光
                ScreenBrightness.modifyBrightness(mainActivity);
                break;
            case SERIAL_PORT_SHUT_DOWN:
                // 显示关机hud
                mainActivity.showShutDownHud();

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
                mainActivity.gpsBar.showAlerting(true);
                break;
            case SERIAL_PORT_ALERT_FAIL:
                mainActivity.gpsBar.showAlerting(false);
                mainActivity.showAlertFailDialog();
                break;
            default:
                super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                break;
        }
    }

}
