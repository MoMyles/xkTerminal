package com.cetcme.xkterminal.Socket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.cetcme.xkterminal.MyClass.Constant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by qiuhong on 16/03/2018.
 */

public class SocketManager {

    private ServerSocket server;
    private Handler handler = null;
    private Context context;

    public SocketManager(Handler handler, Context context){
        this.handler = handler;
        this.context = context;
        int port = Constant.FILE_SOCKET_SERVER_PORT;
        while (port > 9000) {
            try {
                server = new ServerSocket(port);
                break;
            } catch (Exception e) {
                port--;
            }
        }
        SendMessage(1, port);
        Thread receiveFileThread = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    ReceiveFile();
                }
            }
        });
        receiveFileThread.start();
    }
    void SendMessage(int what, Object obj){
        if (handler != null){
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    void ReceiveFile(){
        try{

            Socket name = server.accept();
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();
            SendMessage(0, "正在接收:" + fileName);

            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();
            String fileDirPath = Environment.getExternalStorageDirectory() + File.separator + "船载北斗";
            String savePath = fileDirPath + "/" + fileName;

            File fileDir = new File(fileDirPath);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            FileOutputStream file = new FileOutputStream(savePath, false);
            byte[] buffer = new byte[1024];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1){
                file.write(buffer, 0 ,size);
            }
            file.close();
            dataStream.close();
            data.close();
            SendMessage(0, fileName + "接收完成");

            // 判断文件类型 安装apk
            if (getExtensionName(savePath).equals("apk")) {
                installApp(savePath);
            }
        }catch(Exception e){
            SendMessage(0, "接收错误:\n" + e.getMessage());
        }
    }

    public void SendFile(ArrayList<String> fileName, ArrayList<String> path, String ipAddress, int port){
        try {
            for (int i = 0; i < fileName.size(); i++){
                Socket name = new Socket(ipAddress, port);
                OutputStream outputName = name.getOutputStream();
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
                BufferedWriter bwName = new BufferedWriter(outputWriter);
                bwName.write(fileName.get(i));
                bwName.close();
                outputWriter.close();
                outputName.close();
                name.close();
                SendMessage(0, "正在发送" + fileName.get(i));

                Socket data = new Socket(ipAddress, port);
                OutputStream outputData = data.getOutputStream();
                FileInputStream fileInput = new FileInputStream(path.get(i));
                int size = -1;
                byte[] buffer = new byte[1024];
                while((size = fileInput.read(buffer, 0, 1024)) != -1){
                    outputData.write(buffer, 0, size);
                }
                outputData.close();
                fileInput.close();
                data.close();
                SendMessage(0, fileName.get(i) + "  发送完成");
            }
            SendMessage(0, "所有文件发送完成");
        } catch (Exception e) {
            SendMessage(0, "发送错误:\n" + e.getMessage());
        }
    }

    /**
     * 安装新版本应用
     */
    private void installApp(String FILE_NAME) {
        File appFile = new File(FILE_NAME);
        if(!appFile.exists()) {
            return;
        }
        // 跳转到新版本应用安装页面
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + appFile.toString()), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Java文件操作 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

}
