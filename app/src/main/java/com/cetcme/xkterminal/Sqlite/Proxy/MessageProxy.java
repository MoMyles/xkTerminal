package com.cetcme.xkterminal.Sqlite.Proxy;

import android.database.Cursor;

import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;

import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
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
    public static void setMessageReadById(DbManager db, int id) {
        try {
            MessageBean messageBean = db.findById(MessageBean.class, id);
            if (messageBean != null) {
                messageBean.setRead(true);
            }
            db.saveOrUpdate(messageBean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置短信已读
     * @param db
     * @param sender
     */
    public static void setMessageReadBySender(DbManager db, String sender) {
        try {
            WhereBuilder whereBuilder = WhereBuilder.b("sender", "=", sender);
            whereBuilder.and(WhereBuilder.b("read", "=", false));
            db.update(MessageBean.class, whereBuilder, new KeyValue("read", true));
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
                    .where("isSend","=", isSend)
                    .count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }


    /**
     * 分页查询短信
     * @param db
     * @param isSend
     * @param perPage
     * @param page
     * @return
     */
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


    /**
     * 获取短信
     * @param db
     * @param address
     * @param myNumber
     */
    public static List<MessageBean> getByAddressAndTime(DbManager db, String address, String myNumber, int perPage, String timeBefore) {
        List<MessageBean> list = null;
        try {
//            WhereBuilder whereBuilder;
//            whereBuilder = WhereBuilder.b("send_time", "<", timeBefore);
//            if (address.equals(myNumber)) {
//                whereBuilder.expr(" and (sender = \"" + address + "\" and receiver = \"" + address + "\")");
//            } else {
//                whereBuilder.expr(" and (sender = \"" + address + "\" or receiver = \"" + address + "\")");
//            }

            WhereBuilder timeWhereBuilder;
            timeWhereBuilder = WhereBuilder.b("send_time", "<", DateUtil.String2Date(timeBefore).getTime());

            WhereBuilder whereBuilder;
            whereBuilder = WhereBuilder.b("sender", "=", address);
            if (address.equals(myNumber)) {
                whereBuilder.and("receiver", "=", address);
            } else {
                whereBuilder.or("receiver", "=", address);
            }

            timeWhereBuilder.and(whereBuilder);

            System.out.println(timeWhereBuilder.toString());
            list = db.selector(MessageBean.class)
                    .where(timeWhereBuilder)
                    .limit(perPage)
                    .findAll();

        } catch (DbException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 删除短信
     * @param db
     * @param address
     * @param myNumber
     */
    public static void deleteAllByAddress(DbManager db, String address, String myNumber) {
        try {
            WhereBuilder whereBuilder;
            if (address.equals(myNumber)) {
                whereBuilder = WhereBuilder.b("sender", "=", address);
                whereBuilder.and(WhereBuilder.b("receiver", "=", address));
            } else {
                whereBuilder = WhereBuilder.b("sender", "=", address);
                whereBuilder.or(WhereBuilder.b("receiver", "=", address));
            }
            db.delete(MessageBean.class, whereBuilder);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最后一条短信
     * @param db
     * @param address
     * @param myNumber
     */
    public static MessageBean getLast(DbManager db, String address, String myNumber) {
        MessageBean message = null;
        try {
            WhereBuilder whereBuilder;
            if (address.equals(myNumber)) {
                whereBuilder = WhereBuilder.b("sender", "=", address);
                whereBuilder.and(WhereBuilder.b("receiver", "=", address));
            } else {
                whereBuilder = WhereBuilder.b("sender", "=", address);
                whereBuilder.or(WhereBuilder.b("receiver", "=", address));
            }
            message = db.selector(MessageBean.class).where(whereBuilder).orderBy("send_time", true).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return message;
    }


    /**
     * 获取未读短信内容
     * @param db
     * @param address
     */
    public static long getUnReadCountByAddress(DbManager db, String address) {
        long count = 0;
        try {
            WhereBuilder whereBuilder = WhereBuilder.b("receiver", "=", address);
            whereBuilder.and("isSend", "=", false);
            whereBuilder.and("read", "=", false);

            count = db.selector(MessageBean.class).where(whereBuilder).count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 获取地址集合
     * @param db
     */
    public static List<String> getAddress(DbManager db) {
        List<String> list = new ArrayList<>();
        try {
            Cursor cursor = db.execQuery("select sender from t_message where isSend = 0 group by sender");
            //判断游标是否为空
            if (cursor.moveToFirst()) {
                //遍历游标
                for(int i = 0; i < cursor.getCount(); i++){
                    cursor.moveToPosition(i);
                    //获得ID
                    String sender = cursor.getString(0);
                    //输出用户信息
                    if (!list.contains(sender)) list.add(sender);
                }
            }

            cursor = db.execQuery("select receiver from t_message where isSend = 1 group by receiver ");
            //判断游标是否为空
            if (cursor.moveToFirst()) {
                //遍历游标
                for(int i = 0; i < cursor.getCount(); i++){
                    cursor.moveToPosition(i);
                    //获得ID
                    String receiver = cursor.getString(0);
                    //输出用户信息
                    if (!list.contains(receiver)) list.add(receiver);
                }
            }
            cursor.close();
        } catch (DbException e) {
            e.printStackTrace();
        }

        return list;
    }

}
