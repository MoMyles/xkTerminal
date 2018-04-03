package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.FriendBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

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
    
}
