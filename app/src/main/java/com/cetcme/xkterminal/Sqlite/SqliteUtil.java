package com.cetcme.xkterminal.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 * Created by qiuhong on 03/04/2018.
 */

public class SqliteUtil {

    private static SQLiteDatabase db;

    public SqliteUtil(Context context) {
        db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getPath() + "/sqlite.db",null);
    }

    public void createTable(){
        // 创建表SQL语句
        String stu_table="create table message(_id integer primary key autoincrement, sender text, receiver text, send_time date, content text, read boolean, deleted boolean, isSend boolean, sendOK boolean)";
        // 执行SQL语句
        db.execSQL(stu_table);
    }

    public void insert(String sender, String receiver, Date send_time, String content, Boolean read, Boolean deleted, boolean isSend, boolean sendOK){
        // 实例化常量值
        ContentValues cValue = new ContentValues();
        cValue.put("sender", sender);
        cValue.put("receiver", receiver);
        cValue.put("send_time", send_time.toString());
        cValue.put("content", content);
        cValue.put("read", read);
        cValue.put("deleted", deleted);
        cValue.put("isSend", isSend);
        cValue.put("sendOK", sendOK);
        // 调用insert()方法插入数据
        db.insert("message",null, cValue);
    }

    public void query() {
        //查询获得游标
        Cursor cursor = db.query ("message", null, "read = 1 and deleted = 1",null, null, null, null);

        //判断游标是否为空
        if (cursor.moveToFirst()) {
            //遍历游标
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.move(i);
                //获得ID
                int id = cursor.getInt(0);
                String sender = cursor.getString(1);
                String receiver = cursor.getString(2);
                String send_time = cursor.getString(3);
                String content = cursor.getString(4);
                String read = cursor.getString(5);
                String deleted = cursor.getString(6);
                String isSend = cursor.getString(7);
                String sendOK = cursor.getString(8);
                //输出用户信息
                System.out.println(id + ":" + sender + ":" + receiver + ":" + send_time + ":" + read);
            }
        }
    }

}
