package com.cetcme.xkterminal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

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
import com.cetcme.xkterminal.Socket.SocketManager;
import com.cetcme.xkterminal.Socket.SocketServer;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.db.table.TableEntity;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;


import static com.cetcme.xkterminal.MainActivity.myNumber;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class MyApplication extends Application {

    public MainActivity mainActivity;
    public IDCardActivity idCardActivity;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private boolean messageSendFailed = true;

    private static final int SERIAL_PORT_RECEIVE_NEW_MESSAGE = 0x01;
    private static final int SERIAL_PORT_MESSAGE_SEND_SUCCESS = 0x02;
    private static final int SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM = 0x03;
    private static final int SERIAL_PORT_ALERT_SEND_SUCCESS = 0x04;
    private static final int SERIAL_PORT_SHOW_ALERT_ACTIVITY = 0x05;
    private static final int SERIAL_PORT_RECEIVE_NEW_SIGN = 0x06;
    private static final int SERIAL_PORT_RECEIVE_NEW_ALERT = 0x07;
    private static final int SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS = 0x08;
    private static final int SERIAL_PORT_SHUT_DOWN = 0x09;
    private static final int SERIAL_PORT_ALERT_START = 0x10;
    private static final int SERIAL_PORT_ALERT_FAIL = 0x11;

    // for file pick
    private Handler handler;

    private Toast tipToast;

    private int failedMessageId = 0;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
//        x.Ext.setDebug(BuildConfig.DEBUG); // 开启debug会影响性能

        EventBus.getDefault().register(this);

        try {
            mSerialPort = getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
            ReadThread mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }


        new Thread() {
            @Override
            public void run() {
                new SocketServer().startService();
            }
        }.start();

        tipToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        String str = "[" + format.format(Constant.SYSTEM_DATE) + "]" + msg.obj.toString();
                        tipToast.setText(str);
                        tipToast.show();
                        System.out.println(str);
                        break;
                    case 1:
                        System.out.println("本机IP：" + " 监听端口:" + msg.obj.toString());
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        new SocketManager(handler, getApplicationContext());

        initDb();
    }


    public DbManager db;

    public void initDb(){
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("xkTerminal.db")
                // 不设置dbDir时, 默认存储在app的私有目录.
                //.setDbDir(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"mswcs/db")) //设置数据库.db文件存放的目录,默认为包名下databases目录下
                .setDbVersion(1)//设置数据库版本,每次启动应用时将会检查该版本号,
                //发现数据库版本低于这里设置的值将进行数据库升级并触发DbUpgradeListener
                .setAllowTransaction(true)//设置是否开启事务,默认为false关闭事务
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setTableCreateListener(new DbManager.TableCreateListener() {
                    @Override
                    public void onTableCreated(DbManager db, TableEntity<?> table) {

                    }
                })//设置数据库创建时的Listener
                //设置数据库升级时的Listener,这里可以执行相关数据库表的相关修改,比如alter语句增加字段等
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        // TODO: ...
                        // db.addColumn(...);
                        // db.dropTable(...);
                        // ...
                        // or
                        // db.dropDb();
                        if(newVersion>oldVersion){

                        }
                    }
                });
        db = x.getDb(daoConfig);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(SmsEvent smsEvent) {
        JSONObject receiveJson = smsEvent.getReceiveJson();
        try {
            String apiType = receiveJson.getString("apiType");
            JSONObject jsonObject = new JSONObject();
            switch (apiType) {
                case "sms_list":
                    JSONArray smsList = getSmsList();

                    jsonObject.put("apiType", "sms_list");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("data", smsList);
                    jsonObject.put("userAddress", MainActivity.myNumber);
                    SocketServer.send(jsonObject);

                    break;
                case "sms_detail":
                    String userAddress = receiveJson.getString("userAddress");
                    int countPerPage = receiveJson.getInt("countPerPage");
                    String timeBefore = receiveJson.getString("timeBefore");
                    JSONArray smsDetailStr = getSmsDetail(userAddress, countPerPage, timeBefore);

                    jsonObject.put("apiType", "sms_detail");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("data", smsDetailStr);
                    SocketServer.send(jsonObject);
                    break;

                case "sms_send":
                    final MessageBean message = new MessageBean();
                    message.fromJson(receiveJson.getJSONObject("data"));

                    SharedPreferences sharedPreferences = getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
                    String lastSendTime = sharedPreferences.getString("lastSendTime", "");
                    if (!lastSendTime.isEmpty()) {
                        Long sendDate = DateUtil.parseStringToDate(lastSendTime, DateUtil.DatePattern.YYYYMMDDHHMMSS).getTime();
                        Long now = new Date().getTime();
                        if (now - sendDate <= Constant.MESSAGE_SEND_LIMIT_TIME) {
                            long remainSecond = (Constant.MESSAGE_SEND_LIMIT_TIME - (now - sendDate)) / 1000;
                            // 返回不成功socket
                            Toast.makeText(this, "发送时间间隔不到1分钟，请等待" + remainSecond + "秒", Toast.LENGTH_SHORT).show();

                            JSONObject sendJson = new JSONObject();
                            try {
                                sendJson.put("apiType", "sms_send");
                                sendJson.put("code", 1);
                                sendJson.put("msg", "发送时间间隔不到1分钟，请等待" + remainSecond + "秒");

                                SocketServer.send(sendJson);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }

                    int length = 0;
                    try {
                        length = message.getContent().getBytes("GBK").length;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (Constant.MESSAGE_CONTENT_MAX_LENGTH != 0 && length > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
                        // 返回不成功socket

                        JSONObject sendJson = new JSONObject();
                        try {
                            sendJson.put("apiType", "sms_send");
                            sendJson.put("code", 2);
                            sendJson.put("msg", "内容长度:" + length + ",超出最大值" + Constant.MESSAGE_CONTENT_MAX_LENGTH + "!");

                            SocketServer.send(sendJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                    editor.putString("lastSendTimeSave", DateUtil.parseDateToString(Constant.SYSTEM_DATE, DateUtil.DatePattern.YYYYMMDDHHMMSS));
                    editor.apply(); //提交修改

                    MessageProxy.insert(db, message);
                    failedMessageId = message.getId();

                    byte[] messageBytes = MessageFormat.format(message.getReceiver(), message.getContent(), MessageFormat.MESSAGE_TYPE_NORMAL);
                    sendBytes(messageBytes);
                    System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));

                    messageSendFailed = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (messageSendFailed) {
                                // 返回失败socket
                                JSONObject sendJson = new JSONObject();
                                try {
                                    sendJson.put("apiType", "sms_send");
                                    sendJson.put("code", 1);
                                    sendJson.put("msg", "发送失败");
                                    sendJson.put("id", failedMessageId);

                                    SocketServer.send(sendJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (failedMessageId != 0) {
                                    MessageProxy.setMessageFailed(db, failedMessageId);

                                    if (mainActivity.fragmentName.equals("message") && mainActivity.messageFragment.tg.equals("send")) {
                                        mainActivity.messageFragment.reloadDate();
                                    }
                                }
                            }
                        }
                    }, Constant.MESSAGE_FAIL_TIME);

                    break;
                case "sms_read":
                    final String userAddress1 = receiveJson.getString("userAddress");
                    MessageProxy.setMessageReadBySender(db, userAddress1);
                    mainActivity.modifyGpsBarMessageCount();
                    break;
                case "sms_delete":
                    final String userAddress2 = receiveJson.getString("userAddress");
                    // 删除 所有消息
                    MessageProxy.deleteAllByAddress(db, userAddress2, myNumber);
                    mainActivity.modifyGpsBarMessageCount();
                    break;
                case "set_time":
                    Date date = new Date(receiveJson.getString("time"));
                    if (Math.abs(date.getTime() - Constant.SYSTEM_DATE.getTime()) > 3600 * 1000) {
                        Toast.makeText(getApplicationContext(), "设置时间", Toast.LENGTH_SHORT).show();
                        Constant.SYSTEM_DATE = date;
                    }
                    break;
                case "debug":
                    int orderCode = receiveJson.getInt("code");
                    int orderContent = receiveJson.getInt("content");
                    switch (orderCode) {
                        case 0:
                            mainActivity.gpsBar.setDebugButtonLayoutShow(orderContent == 0);
                            break;
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONArray getSmsList() {
        List<String> userAddresses = MessageProxy.getAddress(db);

        JSONArray smsList = new JSONArray();
        for (String userAddress : userAddresses) {

            MessageBean message = MessageProxy.getLast(db, userAddress, myNumber);
            if (message != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lastSmsContent", message.getContent());
                    jsonObject.put("userAddress", userAddress);
                    jsonObject.put("lastSmsTime", message.getSend_time());
                    jsonObject.put("hasUnread", MessageProxy.getUnReadCountByAddress(db, userAddress) != 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                smsList.put(jsonObject);
            }

        }

        return smsList;
    }

    public JSONArray getSmsDetail(String userAddress, int countPerPage, String timeBefore) {
        List<MessageBean> messages = MessageProxy.getByAddressAndTime(db, userAddress, myNumber, countPerPage, timeBefore);

        JSONArray smsList = new JSONArray();

        for (int i = 0; i < messages.size(); i++) {
            MessageBean message = messages.get(i);
            JSONObject jsonObject = message.toJson();
            smsList.put(jsonObject);
        }
        return smsList;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
			/* Read serial port parameters */
//            SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
//            String path = sp.getString("DEVICE", "");
//            path = "/dev/ttyS3";
//            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
            String path = "/dev/ttyS3";
            int baudrate = Constant.SERIAL_PORT_BAUD_RATE;

			/* Check parameters */
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        b.show();
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    Thread.sleep(10);
                    byte[] buffer = new byte[1];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    byte[] serialBuffer = new byte[100];
    int serialCount = 0;
    boolean hasHead = false;

    protected void onDataReceived(byte[] buffer, int size) {
        serialBuffer[serialCount] = buffer[0];
        serialCount++;
        if (serialCount == 3) {
            String head = Util.bytesGetHead(serialBuffer, 3);
            if (head.equals("$04") || head.equals("$R4") || head.equals("$R1") || head.equals("$R5") || head.equals("$R0") || head.equals("$R6") || head.equals("$R7") || head.equals("$R8")) {
                hasHead = true;
            } else {
                Util.bytesRemoveFirst(serialBuffer, serialCount);
                serialCount--;
            }
        }

        if (hasHead) {
            if (serialCount == 76) {
                serialBuffer = new byte[100];
                serialCount = 0;
                return;
            }
            if (serialBuffer[serialCount - 2] == (byte) 0x0D && serialBuffer[serialCount - 1] == (byte) 0x0A) {
                System.out.println("收到包：" + ConvertUtil.bytesToHexString(serialBuffer));
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putByteArray("bytes", serialBuffer);
                switch (Util.bytesGetHead(serialBuffer, 3)) {
                    case "$04":
                        // 接收短信
                        message.what = SERIAL_PORT_RECEIVE_NEW_MESSAGE;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    default:
                        hasHead = false;
                        Util.bytesRemoveFirst(serialBuffer, serialCount);
                        serialCount--;
                        break;
                }
                hasHead = false;
                serialBuffer = new byte[100];
                serialCount = 0;
            } else if (serialBuffer[serialCount - 1] == (byte) 0x3B) {
                System.out.println("收到包：" + ConvertUtil.bytesToHexString(serialBuffer));
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putByteArray("bytes", serialBuffer);
                switch (Util.bytesGetHead(serialBuffer, 3)) {
                    case "$04":
                        // 接收短信 如果短信内容有分号0x3B 将会进入此处 返回继续接收数据
                        return;
                    case "$R4":
                        // 短信发送成功
                        message.what = SERIAL_PORT_MESSAGE_SEND_SUCCESS;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R1":
                        // 接收时间
                        message.what = SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R5":
                        if (serialCount == 14) {
                            // 紧急报警成功
                            message.what = SERIAL_PORT_ALERT_SEND_SUCCESS;
                        } else if (serialCount == 15) {
                            // 显示报警activity
                            message.what = SERIAL_PORT_SHOW_ALERT_ACTIVITY;
                        } else if (serialCount == 16) {
                            // 增加报警记录，显示收到报警
                            message.what = SERIAL_PORT_RECEIVE_NEW_ALERT;
                        }
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R0":
                        // 接收身份证信息
                        message.what = SERIAL_PORT_RECEIVE_NEW_SIGN;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R6":
                        // 调节背光
                        message.what = SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R7":
                        // 关机
                        message.what = SERIAL_PORT_SHUT_DOWN;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R8":
                        if (serialBuffer[3] == 0x01) {
                            System.out.println("报警中");
                            // 报警中
                            message.what = SERIAL_PORT_ALERT_START;
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        } else if (serialBuffer[3] == 0x02) {
                            System.out.println("报警失败");
                            // 报警失败
                            message.what = SERIAL_PORT_ALERT_FAIL;
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                        break;
                    default:
                        hasHead = false;
                        Util.bytesRemoveFirst(serialBuffer, serialCount);
                        serialCount--;
                        break;
                }
                hasHead = false;
                serialBuffer = new byte[100];
                serialCount = 0;
            }

        }

    }

    public void sendBytes(byte[] buffer) {
        new SendingThread(buffer).start();
        System.out.println("发送包：" + ConvertUtil.bytesToHexString(buffer));
    }

    private class SendingThread extends Thread {

        private byte[] buffer;

        SendingThread(byte[] buffer) {
            this.buffer = buffer;
        }
        @Override
        public void run() {
            try {
                if (mOutputStream != null) {
                    mOutputStream.write(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {//覆盖handleMessage方法
            byte[] bytes = msg.getData().getByteArray("bytes");

            SharedPreferences sharedPreferences = getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器

            switch (msg.what) {//根据收到的消息的what类型处理
                case SERIAL_PORT_RECEIVE_NEW_MESSAGE:
                    // 收到新短信
                    String[] messageStrings = MessageFormat.unFormat(bytes);
                    String address = messageStrings[0];
                    String content = messageStrings[1];
                    String type    = messageStrings[2];

                    // 判断类型 普通短信 还是 救护短信
                    if (type.equals(MessageFormat.MESSAGE_TYPE_RESCURE)) {
                        sendLightOn(true);
                        mainActivity.showRescueDialog(content);
                        mainActivity.addMessage(address, content, true);
                    } else {
                        SoundPlay.playMessageSound(getApplicationContext());
                        mainActivity.addMessage(address, content, false);
                        mainActivity.modifyGpsBarMessageCount();
                        Toast.makeText(getApplicationContext(), "您有新的短信", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SERIAL_PORT_MESSAGE_SEND_SUCCESS:
                    // 短信发送成功
                    Toast.makeText(getApplicationContext(), "短信发送成功", Toast.LENGTH_SHORT).show();

                    String lastSendTimeSave = sharedPreferences.getString("lastSendTimeSave", "");
                    editor.putString("lastSendTime", lastSendTimeSave);
                    editor.apply(); //提交修改

                    // 用于去掉2秒后显示发送失败提示
                    mainActivity.messageSendFailed = false;
                    messageSendFailed = false;

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

                    int originalTimeZone = PreferencesUtils.getInt(getApplicationContext(), "time_zone");
                    if (originalTimeZone == -1) originalTimeZone = Constant.TIME_ZONE;

                    long rightTime = date.getTime() + (originalTimeZone - 12) * 3600 * 1000;
                    Date rightDate = new Date(rightTime);

                    SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
                    if (Integer.parseInt(yearSdf.format(rightDate)) >= 2018) {
                        System.out.println("设置系统时间");
                        Constant.SYSTEM_DATE = rightDate;
                    }
                    System.out.println(rightDate);

                    int myNumber = Util.bytesToInt2(ByteUtil.subBytes(bytes, 17, 21), 0);
                    PreferencesUtils.putString(getApplicationContext(), "myNumber", myNumber + "");
                    MainActivity.myNumber = myNumber + "";
                    System.out.println("myNumber: " + myNumber);

                    String status = Util.byteToBit(ByteUtil.subBytes(bytes, 21, 22)[0]);
                    boolean gpsStatus = status.charAt(7) == '1';
                    mainActivity.gpsBar.setGPSStatus(gpsStatus);
                    String communication_from = status.charAt(6) == '1' ? "北斗" : "GPRS";
                    PreferencesUtils.putString(getApplicationContext(), "communication_from", communication_from);
                    break;
                case SERIAL_PORT_ALERT_SEND_SUCCESS:
                    // 报警发送成功
                    mainActivity.gpsBar.showAlerting(false);
                    Toast.makeText(getApplicationContext(), "遇险报警发送成功", Toast.LENGTH_SHORT).show();
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

                    PreferencesUtils.putBoolean(getApplicationContext(), "homePageAlertView", true);
                    SoundPlay.startAlertSound(getApplicationContext());

                    byte[] alertBytes = ByteUtil.subBytes(bytes, 11, 13);
                    if (alertBytes[0] == 0x02 && alertBytes[1] == 0x00) {
                        // 落水报警
                        Toast.makeText(getApplicationContext(), "收到落水报警", Toast.LENGTH_SHORT).show();
                        mainActivity.addAlertLog("落水");
                    } else if (alertBytes[0] == 0x10 && alertBytes[1] == 0x00){
                        // 解除报警
                        PreferencesUtils.putBoolean(getApplicationContext(), "homePageAlertView", false);
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
                    sendBytes(sendBytes);
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
    };

    public void sendLightOn(boolean on) {
        System.out.println("控制灯：" + on);
        byte[] bytes;
        if (on) {
            bytes ="$08".getBytes();
        } else {
            bytes ="$09".getBytes();
        }
        bytes = ByteUtil.byteMerger(bytes, new byte[] {0x01});
        bytes = ByteUtil.byteMerger(bytes, "\r\n".getBytes());
        sendBytes(bytes);
    }
}
