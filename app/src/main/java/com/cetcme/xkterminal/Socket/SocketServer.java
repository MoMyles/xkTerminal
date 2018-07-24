package com.cetcme.xkterminal.Socket;

import android.content.Context;

import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;

import org.greenrobot.eventbus.EventBus;
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

/**
 * 逻辑：
 * 允许单台手机登陆，另一台登陆的时候老连接断开
 * 新设备登陆的时候 终端弹出是否允许登陆的框 点击允许之后 静态socket重新赋值
 */
public class SocketServer {

    private static int BUFFER_SIZE = 1024 * 1024;

    private static Socket socket;
    private static Socket waitToLoginSocket;

    /**
     * 启动服务监听，等待客户端连接
     */
    public void startService(Context context) {
        try {
            // 创建ServerSocket
            ServerSocket serverSocket = new ServerSocket(Constant.SOCKET_SERVER_PORT);
            System.out.println("--开启服务器，监听端口 9999--");

            // 监听端口，等待客户端连接
            while (true) {
                System.out.println("--等待客户端连接--");
                Socket socket = serverSocket.accept(); //等待客户端连接
                System.out.println("得到客户端连接：" + socket);
//                if (this.socket != null) {
//                    System.out.println("关闭老客户端: " + socket);
//                    this.socket.close();
//                }
//                this.socket = socket;
//                startReader(socket);
                waitToLoginSocket = socket;

                // 提示登陆
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "check_login");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void allowLogin() {
        try {
            if (socket != null) {
                System.out.println("关闭老客户端: " + socket);
                socket.close();
            }
            socket = waitToLoginSocket;
            waitToLoginSocket = null;
            startReader(socket);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apiType", "user_login");
            jsonObject.put("code", 0);
            send(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void denyLogin() {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("apiType", "user_login");
            jsonObject.put("code", 1);
            jsonObject.put("msg", "终端拒绝手机登陆");

            new Thread() {
                @Override
                public void run() {

                    try {
                        System.out.println("*to send*");
                        // socket.getInputStream()
                        if (waitToLoginSocket == null) {
                            return;
                        }
                        OutputStreamWriter writer = new OutputStreamWriter(waitToLoginSocket.getOutputStream());

                        writer.write(jsonObject.toString());
                        writer.flush();

                        System.out.println("****send: " + jsonObject.toString());

                        waitToLoginSocket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从参数的Socket里获取最新的消息
     */
    private static void startReader(final Socket socket) {

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

                    System.out.println("****send "+ socket.getInetAddress() + " : " + json.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}