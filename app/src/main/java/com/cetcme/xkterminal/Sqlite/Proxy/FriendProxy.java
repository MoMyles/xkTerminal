package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.FriendBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

/**
 * Created by qiuhong on 03/04/2018.
 */

public class FriendProxy {

    public static void insert(DbManager db, String name, String number) {
        FriendBean friend = new FriendBean();
        friend.setName(name);
        friend.setNumber(number);

        try {
            db.saveBindingId(friend);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void insert(DbManager db, FriendBean friend) {
        try {
            db.saveBindingId(friend);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询全部好友
     * @param db
     * @return
     */
    public static List<FriendBean> getAll(DbManager db) {
        List<FriendBean> list = null;
        try {
            list = db.selector(FriendBean.class)
                    .findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 删除好友
     * @param db
     * @return
     */
    public static void deleteById(DbManager db, int id) {
        try {
            db.deleteById(FriendBean.class, id);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    
}
