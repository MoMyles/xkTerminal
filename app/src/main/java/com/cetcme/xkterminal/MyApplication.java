package com.cetcme.xkterminal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
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
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.MyClass.Constant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class MyApplication extends Application {

    public MainActivity mainActivity;

    public Realm realm;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    @Override
    public void onCreate() {
        super.onCreate();
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 测试接收
//                testReceive(1);
            }
        },2000);

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
                frameData = ByteUtil.byteMerger(frameData, "*hh".getBytes());
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
        System.out.println(ConvertUtil.bytesToHexString(buffer));
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
                        if (serialCount == 18) {
                            // 紧急报警成功
                            message.what = 0x04;
                        } else if (serialCount == 17) {
                            // 显示报警activity
                            message.what = 0x05;
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
            switch (msg.what) {//根据收到的消息的what类型处理
                case 0x01:
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
                    break;
                case 0x03:
                    // 接收时间

                    break;
                case 0x04:
                    // 报警发送成功
                    Toast.makeText(getApplicationContext(), "遇险报警发送成功", Toast.LENGTH_SHORT).show();
                    break;
                case 0x05:
                    // 显示报警activity
                    mainActivity.showDangerDialog();
                    break;
                case 0x06:
                    // 接收身份证信息
                    String[] idStrings = SignFormat.unFormat(bytes);
                    String id = idStrings[0];
                    String name = idStrings[1];
                    mainActivity.showIDCardDialog(id, name);
                    break;
                default:
                    super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                    break;
            }
        }
    };
}
