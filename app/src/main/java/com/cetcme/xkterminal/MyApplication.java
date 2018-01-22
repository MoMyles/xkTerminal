package com.cetcme.xkterminal;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.Util;

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

        byte[] messageBytes = MessageFormat.format("123456", "我是第一条的短信。。。。");
        sendBytes(messageBytes);
//        System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));
        messageBytes = MessageFormat.format("123456", "我是第二条的短信!!!!");
        sendBytes(messageBytes);
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
                    Thread.sleep(100);
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
                switch (Util.bytesGetHead(serialBuffer, 3)) {
                    case "$04":
                        // 接收短信
                        String[] messageStrings = MessageFormat.unFormat(buffer);
                        String address = messageStrings[0];
                        String content = messageStrings[1];
                        mainActivity.addMessage(address, content);
                        mainActivity.modifyGpsBarMessageCount();
                        break;
                    case "$R4":
                        // 短信发送成功
                        Toast.makeText(this, "短信发送成功", Toast.LENGTH_SHORT).show();
                        break;
                    case "$R1":
                        // 接收时间

                        break;
                    case "$R5":

                        if (size == 17) {
                            // 紧急报警成功
                            Toast.makeText(this, "遇险报警发送成功", Toast.LENGTH_SHORT).show();
                        } else if (size == 16) {
                            // 显示报警activity
                            mainActivity.showDangerDialog();
                        }
                        break;
                    case "$R0":
                        // 接收身份证信息
                        String[] idStrings = MessageFormat.unFormat(buffer);
                        String id = idStrings[0];
                        String name = idStrings[1];
                        mainActivity.showIDCardDialog(id, name);
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

    private void unFormatData(byte[] buffer, int size) {
        if (buffer[0] == (byte) 0x24 && buffer[1] == (byte) 0x30) {
            String[] strings = MessageFormat.unFormat(buffer);
            String targetAddress = strings[0];
            String messageContent = strings[1];
            System.out.println("收到来自 " + targetAddress + " 的短信，内容是： " + messageContent);
        }
    }

    public void sendBytes(byte[] buffer) {
        new SendingThread(buffer).start();
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
}
