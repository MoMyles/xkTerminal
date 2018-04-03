package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.Date;
import java.util.List;

/**
 * Created by qiuhong on 03/04/2018.
 */

public class MessageProxy {

    public static void insert(DbManager db, String sender, String receiver, String content, Date send_time, boolean read, boolean deleted, boolean isSend, boolean sendOK) {
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
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void insert(DbManager db, MessageBean message) {
        try {
            db.saveBindingId(message);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置短信发送失败
     * @param db
     * @param id
     */
    public static void setMessageFailed(DbManager db, int id) {
        try {
            MessageBean messageBean = db.selector(MessageBean.class).where("id", "=", id).findFirst();
            if (messageBean != null) {
                messageBean.setSendOK(false);
            }
            db.saveOrUpdate(messageBean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置短信已读
     * @param db
     * @param id
     */
    public static void setMessageRead(DbManager db, int id) {
        try {
            MessageBean messageBean = db.selector(MessageBean.class).where("id", "=", id).findFirst();
            if (messageBean != null) {
                messageBean.setRead(true);
            }
            db.saveOrUpdate(messageBean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取未读短信数量
     * @param db
     * @param receiver
     * @return
     */
    public static long getUnreadMessageCount(DbManager db, String receiver) {
        long count = 0;
        try {
            count = db.selector(MessageBean.class)
                    .where("receiver","=", receiver)
                    .where(WhereBuilder.b("deleted","=","0"))
                    .where(WhereBuilder.b("isSend","=","0"))
                    .where(WhereBuilder.b("read","=","0"))
                    .count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 获取短信数量
     * @param db
     * @param isSend
     * @return
     */
    public static long getCount(DbManager db, boolean isSend) {
        long count = 0;
        try {
            count = db.selector(MessageBean.class)
                    .where("isSend","=", isSend ? 1 : 0)
                    .count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }


    public static List<MessageBean> getByPage(DbManager db, boolean isSend, int perPage, int page) {
        List<MessageBean> list = null;
        try {
            list = db.selector(MessageBean.class)
                    .where("isSend","=", isSend)
                    .and(WhereBuilder.b("deleted","=",false))
                    .limit(perPage)
                    .offset(page * perPage)
                    .orderBy("send_time", true)
                    .findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }


    public static List<MessageBean> queryByReceiver(DbManager db, String receiver) {
        List<MessageBean> list = null;
        try {
            list = db.selector(MessageBean.class)
                    .where("receiver","=", receiver)
                    .and(WhereBuilder.b("deleted","=","0"))
                    .findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<MessageBean> queryBySender(DbManager db, String sender) {
        List<MessageBean> list = null;
        try {
            list = db.selector(MessageBean.class)
                    .where("sender","=", sender)
                    .and(WhereBuilder.b("deleted","=","0")).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }

}
