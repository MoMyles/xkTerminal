package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.SignBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Date;

/**
 * Created by qiuhong on 03/04/2018.
 */

public class SignProxy {

    public static void insert(DbManager db, String idCard, String name, Date time,boolean deleted) {
        SignBean sign = new SignBean();
        sign.setIdCard(idCard);
        sign.setName(name);
        sign.setTime(time);
        sign.setDeleted(deleted);

        try {
            db.saveBindingId(sign);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void insert(DbManager db, SignBean sign) {
        try {
            db.saveBindingId(sign);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    
}
