package com.cetcme.xkterminal.Navigation;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.qiuhong.qhlibrary.Dialog.QHDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by qiuhong on 9/18/16.
 */
public class FileUtil {



    public static String FILE_PATH = Environment.getExternalStorageDirectory() + "/0yima_routes";

    public static void saveFile(String name, String data) {

        File filePath = new File(FILE_PATH);
        if(!filePath.exists()) {
            filePath.mkdir();
        }

        try {
            FileOutputStream outStream = new FileOutputStream(new File(FILE_PATH  + name));
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF-8");
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendData(String name, String data) {

        File filePath = new File(FILE_PATH);
        if(!filePath.exists()) {
            filePath.mkdir();
        }

        try {
            FileOutputStream outStream = new FileOutputStream(new File(FILE_PATH  + name), true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF-8");
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String name) {
        FileInputStream fis = null;
        StringBuffer sBuf = new StringBuffer();
        try {
            fis = new FileInputStream(FILE_PATH + name);
            int len;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                sBuf.append(new String(buf,0,len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sBuf.toString();
    }

    public static List<Map<String, Object>> getFilesData() {
        File f = new File(FILE_PATH);
        File[] files = f.listFiles();

        if (files == null) {
            return null;
        }

        List<Map<String, Object>> filesData = new ArrayList<>();
        for (File file: files) {
            Map<String, Object> map = new Hashtable<>();
            map.put("fileName", file.getName());
            map.put("lastModifyTime", stampToDate(file.lastModified()));
            map.put("lastModifyStamp", file.lastModified());
            map.put("fileLength", (new BigDecimal(file.length() / 1024f)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "KB");
            filesData.add(map);
        }
        return filesData;
    }

    public static String getLastFileName() {
        File f = new File(FILE_PATH);
        File[] files = f.listFiles();
        return files[files.length - 1].getName();
    }

    public static void renameFile(Context context, String oldName, String newName) {
        if(!oldName.equals(newName)){//新的文件名和以前文件名不同时,才有必要进行重命名
            File oldFile = new File(FILE_PATH + "/" + oldName);
            File newFile = new File(FILE_PATH + "/" + newName);
            if(!oldFile.exists()){
                return;  //重命名文件不存在
            }
            if(newFile.exists()) {//若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                System.out.println(newName + "已经存在！");
                new QHDialog(context, "提示", "文件名已存在!").show();
            } else{
                oldFile.renameTo(newFile);
            }
        }else{
            System.out.println("新文件名和旧文件名相同...");
        }
    }

    public static void deleteFile(String fileName) {
        File file = new File(FILE_PATH, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String stampToDate(long s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        long lt = new Long(s);
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }

}
