package com.cetcme.xkterminal.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.DataHandler;
import com.cetcme.xkterminal.MyApplication;

import java.io.InputStream;
import java.util.ArrayList;

import android_serialport_api.SerialPort;

public class DataReadThread extends Thread {


    private Handler mHandler;
    private InputStream inputStream;
    private final ArrayList<Byte> BYTE_LIST = new ArrayList<>();
    private boolean canRead = true;

    public DataReadThread(MyApplication context, SerialPort port) {
        if (port != null) {
            this.inputStream = port.getInputStream();
        }
        mHandler = new DataHandler(context);
    }

    public void close() {
        canRead = false;
    }


    @Override
    public void run() {
        while (canRead) {
            try {
                Thread.sleep(1);
                if (inputStream != null) {
                    int size = inputStream.available();
                    byte[] byts = new byte[size];
                    inputStream.read(byts);
                    for (int i = 0; i < size; i++) {
                        analysisData(byts[i]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void analysisData(byte b) {
        BYTE_LIST.add(b);
        int size = BYTE_LIST.size();
        if (size > 2) {
            byte[] byts = list2array(BYTE_LIST, size);
            if (hasHead("$04", byts)) {
                if (hasTail(byts)) {
                    int xIndex = size;
                    for (int i = size - 1; i > 0; i--) {
                        if (byts[i] == 0x2A) {
                            xIndex = i;
                            break;
                        }
                    }
                    if (xIndex == size) {// 未找到*
                        return;
                    }
                    int checkSum = Util.computeCheckSum(byts, 3, xIndex);
                    byte check = (byte) checkSum;
                    if (check == byts[xIndex + 1]) {
                        //校验成功

                        sendMessage(byts, DataHandler.SERIAL_PORT_RECEIVE_NEW_MESSAGE);

                        BYTE_LIST.clear();
                    }
                }
            } else if (hasHead("$R4", byts)) {
                if (hasBan(byts)) {
                    sendMessage(byts, DataHandler.SERIAL_PORT_MESSAGE_SEND_SUCCESS);
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R1", byts)) {
                if (hasBan(byts)) {
                    if (byts[size - 3] == 0x2A)
                    if (size == 25) {
                        sendMessage(byts, DataHandler.SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM);
                    } else if (size == 20) {
                        sendMessage(byts, DataHandler.SERIAL_PORT_TIME);
                    }
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R2", byts)) {
                if (hasBan(byts)) {
                    sendMessage(byts, DataHandler.SERIAL_PORT_ID_EDIT_OK);
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R5", byts)) {
                if (hasBan(byts)) {
                    if (size == 14) {
                        // 紧急报警成功
                        sendMessage(byts, DataHandler.SERIAL_PORT_ALERT_SEND_SUCCESS);
                    } else if (size == 15) {
                        // 显示报警activity
                        sendMessage(byts, DataHandler.SERIAL_PORT_SHOW_ALERT_ACTIVITY);
                    } else if (size == 16) {
                        // 增加报警记录，显示收到报警
                        sendMessage(byts, DataHandler.SERIAL_PORT_RECEIVE_NEW_ALERT);
                    }
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R0", byts)) {
                if (hasBan(byts)) {
                    sendMessage(byts, DataHandler.SERIAL_PORT_RECEIVE_NEW_SIGN);
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R6", byts)) {
                if (hasBan(byts)) {
                    sendMessage(byts, DataHandler.SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS);
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R7", byts)) {
                if (hasBan(byts)) {
                    sendMessage(byts, DataHandler.SERIAL_PORT_SHUT_DOWN);
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$R8", byts)) {
                if (hasBan(byts)) {
                    if (byts[3] == 0x01) {
                        sendMessage(byts, DataHandler.SERIAL_PORT_ALERT_START);
                    } else if (byts[3] == 0x02) {
                        sendMessage(byts, DataHandler.SERIAL_PORT_ALERT_FAIL);
                    }
                    BYTE_LIST.clear();
                }
            } else if (hasHead("$RA", byts)) {
                if (hasBan(byts)) {
                    sendMessage(byts, DataHandler.SERIAL_PORT_CHECK);
                    BYTE_LIST.clear();
                }
            } else {
                BYTE_LIST.remove(0);// 没有协议头清楚数据
            }
        }

        if (size > 1024) {// 错误数据过多，清除
            BYTE_LIST.clear();
        }
    }

    private void sendMessage(byte[] byts, int type) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putByteArray("bytes", byts);
        message.what = type;
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private static byte[] list2array(ArrayList<Byte> byteList, int size) {
        byte[] tmp = new byte[size];
        for (int i = 0; i < size; i++) {
            tmp[i] = byteList.get(i) == null ? 0 : byteList.get(i);
        }
        return tmp;
    }

    private static boolean hasHead(String string, byte[] byts) {
        if (byts.length < 3)
            return false;
        if (string.length() < 3)
            return false;
        if (byts.length < string.length())
            return false;
        int headLen = string.length();
        boolean flag = true;
        for (int i = 0; i < headLen; i++) {
            flag = string.charAt(i) == byts[i];
            if (!flag) {
                break;
            }
        }
        return flag;
    }

    // \r\n结尾
    private static boolean hasTail(byte[] byts) {
        int len = byts.length;
        boolean flag = false;
        flag = (byts[len - 2] == 0x0D) && (byts[len - 1] == 0x0A);
        return flag;
    }

    // 封号结尾
    private static boolean hasBan(byte[] byts) {
        int len = byts.length;
        return byts[len - 1] == 0x3B;
    }
}
