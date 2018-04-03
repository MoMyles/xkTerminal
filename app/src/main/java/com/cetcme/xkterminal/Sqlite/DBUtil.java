package com.cetcme.xkterminal.Sqlite;

import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.Date;
import java.util.List;

/**
 * Created by qiuhong on 03/04/2018.
 */

public class DBUtil {

    public static void insert(DbManager db, String sender, String receiver, Date send_time, String content, boolean read, boolean deleted, boolean isSend, boolean sendOK) {
        MessageBean message = new MessageBean();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSend_time(send_time);
        message.setContent(content);
        message.setRead(read);
        message.setDeleted(deleted);
        message.setSend(isSend);
        message.setSendOK(sendOK);

        try {
            db.saveBindingId(message);
//            db.saveOrUpdate(message);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void query(DbManager db) {
        List<MessageBean> list = null;
        try {
            list = db.selector(MessageBean.class).where("receiver","=", "123456").where(WhereBuilder.b("deleted","=","1")).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        for (MessageBean message : list) {
            System.out.println(message.toString());
        }
    }
}