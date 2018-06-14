package com.cetcme.xkterminal.Socket;

import android.app.Application;

import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by qiuhong on 28/11/2016.
 */

public class SocketServer {

//    public static void main(String[] args) {
//        startService();
//    }
    private static int BUFFER_SIZE = 1024 * 1024;

    /**
     * 启动服务监听，等待客户端连接
     */
    public void startService() {
        try {
            // 创建ServerSocket
            ServerSocket serverSocket = new ServerSocket(Constant.SOCKET_SERVER_PORT);
            System.out.println("--开启服务器，监听端口 9999--");

            // 监听端口，等待客户端连接
            while (true) {
                System.out.println("--等待客户端连接--");
                socket = serverSocket.accept(); //等待客户端连接
                System.out.println("得到客户端连接：" + socket);
                startReader(socket);
                
                // 提示登陆
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "login");
                    EventBus.getDefault().post(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 从参数的Socket里获取最新的消息
     */
    private void startReader(final Socket socket) {

        new Thread() {
            @Override
            public void run() {
                try {
                    final InetAddress address = socket.getInetAddress();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    while (true) {

                        System.out.println("*等待客户端数据*");

                        // 读取数据
                        char[] data = new char[BUFFER_SIZE];
                        int len = br.read(data);
                        if (len != -1) {
                            String rexml = String.valueOf(data, 0, len);
                            System.out.println("获取到客户端的信息：" + address + " ");
                            System.out.println(rexml);
                            try {
                                JSONObject receiveJson = new JSONObject(rexml);
                                EventBus.getDefault().post(new SmsEvent(receiveJson));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            socket.close();
                            return;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    static Socket socket;

    /**
     * 发送消息
     */
    public static void send(final JSONObject json) {
        new Thread() {
            @Override
            public void run() {

                try {
                    System.out.println("*to send*");
                    // socket.getInputStream()
                    if (socket == null) {
                        return;
                    }
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

                    writer.write(json.toString());
                    writer.flush();

                    System.out.println("****send: " + json.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}