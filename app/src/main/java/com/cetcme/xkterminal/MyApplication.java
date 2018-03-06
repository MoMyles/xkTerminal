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
import android.util.Log;
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
import com.cetcme.xkterminal.Socket.SocketServer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiuhong.qhlibrary.Dialog.QHDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.cetcme.xkterminal.MainActivity.myNumber;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class MyApplication extends Application {

    public MainActivity mainActivity;
    public IDCardActivity idCardActivity;

    public Realm realm;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("myrealm.realm").build();
        Realm.setDefaultConfiguration(config);

        realm = Realm.getDefaultInstance();

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

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // 测试接收
//                testReceive(1);
//            }
//        },2000);

        new Thread() {
            @Override
            public void run() {
                new SocketServer().startService();
            }
        }.start();

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
                    JSONArray smsDetailStr = getSmsDetail(userAddress);

                    jsonObject.put("apiType", "sms_detail");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("data", smsDetailStr);
                    SocketServer.send(jsonObject);
                    break;

                case "sms_send":
                    final com.cetcme.xkterminal.RealmModels.Message message = new com.cetcme.xkterminal.RealmModels.Message();
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
                            sendJson.put("msg", "短信内容长度超出最大值（" + Constant.MESSAGE_CONTENT_MAX_LENGTH + "）！");

                            SocketServer.send(sendJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                    editor.putString("lastSendTimeSave", DateUtil.parseDateToString(new Date(), DateUtil.DatePattern.YYYYMMDDHHMMSS));
                    editor.apply(); //提交修改

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            com.cetcme.xkterminal.RealmModels.Message newMessage = realm.createObject(com.cetcme.xkterminal.RealmModels.Message.class);
                            newMessage.setSender(myNumber);
                            newMessage.setReceiver(message.getReceiver());
                            newMessage.setContent(message.getContent());
                            newMessage.setDeleted(false);
                            newMessage.setSend_time(message.getSend_time());
                            newMessage.setRead(true);
                            newMessage.setSend(true);
                        }
                    });

                    byte[] messageBytes = MessageFormat.format(message.getReceiver(), message.getContent());
                    sendBytes(messageBytes);
                    System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));

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
                case "sms_read":
                    final String userAddress1 = receiveJson.getString("userAddress");
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            //先查找后得到User对象
                            RealmResults<com.cetcme.xkterminal.RealmModels.Message> messages = realm.where(com.cetcme.xkterminal.RealmModels.Message.class)
                                    .equalTo("sender", userAddress1)
                                    .equalTo("read", false)
                                    .findAll();
                            for (com.cetcme.xkterminal.RealmModels.Message message : messages) {
                                message.setRead(true);
                            }
                            mainActivity.modifyGpsBarMessageCount();
                        }
                    });
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONArray getSmsList() {
        RealmResults<com.cetcme.xkterminal.RealmModels.Message> messages = realm.where(com.cetcme.xkterminal.RealmModels.Message.class)
                .findAll();
        messages = messages.sort("send_time", Sort.DESCENDING);

        ArrayList<String> userAddresses = new ArrayList<>();
        for (com.cetcme.xkterminal.RealmModels.Message message: messages) {
            if (message.isSend() && !userAddresses.contains(message.getReceiver())) userAddresses.add(message.getReceiver());
            if (!message.isSend() && !userAddresses.contains(message.getSender())) userAddresses.add(message.getSender());
        }

        JSONArray smsList = new JSONArray();
        for (String userAddress : userAddresses) {
            RealmQuery<com.cetcme.xkterminal.RealmModels.Message> query = realm.where(com.cetcme.xkterminal.RealmModels.Message.class);
            query.equalTo("receiver", userAddress);
            if (userAddress.equals(myNumber)) {
                query.equalTo("sender", userAddress);
            } else {
                query.or().equalTo("sender", userAddress);
            }
            query.sort("send_time", Sort.ASCENDING);
            RealmResults<com.cetcme.xkterminal.RealmModels.Message> smses = query.findAll();
            com.cetcme.xkterminal.RealmModels.Message message = smses.last();

            RealmQuery<com.cetcme.xkterminal.RealmModels.Message> unreadQuery = realm.where(com.cetcme.xkterminal.RealmModels.Message.class);
            unreadQuery.equalTo("receiver", userAddress);
            unreadQuery.equalTo("read", false);
            if (userAddress.equals(myNumber)) {
                unreadQuery.equalTo("sender", userAddress);
            } else {
                unreadQuery.or().equalTo("sender", userAddress);
            }
            RealmResults<com.cetcme.xkterminal.RealmModels.Message> unreadSmses = unreadQuery.findAll();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("lastSmsContent", message.getContent());
                jsonObject.put("userAddress", userAddress);
                jsonObject.put("lastSmsTime", message.getSend_time());
                jsonObject.put("hasUnread", unreadSmses.size() != 0);
                System.out.println(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            smsList.put(jsonObject);
        }

//        System.out.println("+++++smsList" + smsList);
        return smsList;
    }

    public JSONArray getSmsDetail(String userAddress) {
        RealmResults<com.cetcme.xkterminal.RealmModels.Message> messages;

        if (userAddress.equals(myNumber)) {
            messages = realm.where(com.cetcme.xkterminal.RealmModels.Message.class)
                    .equalTo("sender", userAddress)
                    .equalTo("receiver", userAddress)
                    .findAll();
        } else {
            messages = realm.where(com.cetcme.xkterminal.RealmModels.Message.class)
                    .equalTo("sender", userAddress)
                    .or().equalTo("receiver", userAddress)
                    .findAll();
        }

        messages = messages.sort("send_time", Sort.ASCENDING);

        JSONArray smsList = new JSONArray();
        for (com.cetcme.xkterminal.RealmModels.Message message : messages) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", message.getId());
                jsonObject.put("sender", message.getSender());
                jsonObject.put("receiver", message.getReceiver());
                jsonObject.put("send_time", message.getSend_time());
                jsonObject.put("content", message.getContent());
                jsonObject.put("read", message.isRead());
                jsonObject.put("deleted", message.isDeleted());
                jsonObject.put("isSend", message.isSend());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            smsList.put(jsonObject);
        }
        return smsList;
    }

    private void testReceive(int i) {
        switch (i) {
            case 0:
                // 测试接收短信
                byte[] messageBytes = MessageFormat.format("123456", "我是第一条的短信。。。。");
                sendBytes(messageBytes);
                messageBytes = MessageFormat.format("123456", "我是第二条的短信!!!!");
                sendBytes(messageBytes);
                break;
            case 1:
                // 测试接收身份证信息
                sendBytes(SignFormat.format());
                break;
            case 2:
                // 测试接收报警
                byte[] frameData = "$R5".getBytes();
                frameData = ByteUtil.byteMerger(frameData, "12345678".getBytes());
//                frameData = ByteUtil.byteMerger(frameData, new byte[]{(byte) 0x00});
                frameData = ByteUtil.byteMerger(frameData, "OK".getBytes());
                frameData = ByteUtil.byteMerger(frameData, "*h".getBytes());
                frameData = ByteUtil.byteMerger(frameData, new byte[]{(byte) 0x0D, (byte) 0x0A});
                sendBytes(frameData);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTerminate() {
        Log.e("Application", "onTerminate: ==============");
        super.onTerminate();
        realm.close();
    }


    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
			/* Read serial port parameters */
            SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
            String path = sp.getString("DEVICE", "");
            path = "/dev/ttyS3";
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
            baudrate = 9600;

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
            if (head.equals("$04") || head.equals("$R4") || head.equals("$R1") || head.equals("$R5") || head.equals("$R0")) {
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
                        message.what = 0x01;
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
                    case "$R4":
                        // 短信发送成功
                        message.what = 0x02;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R1":
                        // 接收时间
                        message.what = 0x03;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R5":
                        if (serialCount == 14) {
                            // 紧急报警成功
                            message.what = 0x04;
                        } else if (serialCount == 15) {
                            // 显示报警activity
                            message.what = 0x05;
                        } else if (serialCount == 16) {
                            // todo: 增加报警记录，显示收到报警
                            message.what = 0x07;
                        }
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R0":
                        // 接收身份证信息
                        message.what = 0x06;
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
                case 0x01:
                    // 收到新短信
                    String[] messageStrings = MessageFormat.unFormat(bytes);
                    String address = messageStrings[0];
                    String content = messageStrings[1];
                    mainActivity.addMessage(address, content);
                    mainActivity.modifyGpsBarMessageCount();
                    Toast.makeText(getApplicationContext(), "您有新的短信", Toast.LENGTH_SHORT).show();
                    break;
                case 0x02:
                    // 短信发送成功
                    Toast.makeText(getApplicationContext(), "短信发送成功", Toast.LENGTH_SHORT).show();


                    String lastSendTimeSave = sharedPreferences.getString("lastSendTimeSave", "");
                    editor.putString("lastSendTime", lastSendTimeSave);
                    editor.apply(); //提交修改

                    break;
                case 0x03:
                    // 接收时间
                    int year = ByteUtil.subBytes(bytes, 11, 12)[0]  & 0xFF;
                    int month = ByteUtil.subBytes(bytes, 12, 13)[0]  & 0xFF;
                    int day = ByteUtil.subBytes(bytes, 13, 14)[0]  & 0xFF;
                    int hour = ByteUtil.subBytes(bytes, 14, 15)[0]  & 0xFF;
                    int minute = ByteUtil.subBytes(bytes, 15, 16)[0]  & 0xFF;
                    int second = ByteUtil.subBytes(bytes, 16, 17)[0]  & 0xFF;
                    String dateStr = "20" + year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
                    Date date = DateUtil.parseStringToDate(dateStr);
                    System.out.println(date);

                    int myNumber = Util.bytesToInt2(ByteUtil.subBytes(bytes, 17, 21), 0);
                    PreferencesUtils.putString(getApplicationContext(), "myNumber", myNumber + "");
                    System.out.println("myNumber: " + myNumber);

                    String status = Util.byteToBit(ByteUtil.subBytes(bytes, 21, 22)[0]);
                    boolean gpsStatus = status.charAt(7) == '1';
                    mainActivity.gpsBar.setGPSStatus(gpsStatus);
                    String communication_from = status.charAt(6) == '1' ? "北斗" : "GPRS";
                    PreferencesUtils.putString(getApplicationContext(), "communication_from", communication_from);
                    break;
                case 0x04:
                    // 报警发送成功
                    Toast.makeText(getApplicationContext(), "遇险报警发送成功", Toast.LENGTH_SHORT).show();
                    break;
                case 0x05:
                    // 显示报警activity
                    mainActivity.showDangerDialog();
                    break;
                case 0x07:
                    // todo test: 增加报警记录，显示收到报警
                    Toast.makeText(getApplicationContext(), "收到遇险报警", Toast.LENGTH_SHORT).show();
                    mainActivity.addAlertLog("");
                    break;
                case 0x06:
                    // 接收身份证信息
                    String[] idStrings = SignFormat.unFormat(bytes);
                    String id = idStrings[0];
                    String name = idStrings[1];
                    String nation = "--";
                    String idAddress = "浙江省嘉兴市南湖区xx小区xx幢xx室";
                    mainActivity.showIDCardDialog(id, name, nation, idAddress);
                    break;
                default:
                    super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                    break;
            }
        }
    };
}
