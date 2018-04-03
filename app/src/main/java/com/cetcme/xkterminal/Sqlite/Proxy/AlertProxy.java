package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.AlertBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Date;

/**
 * Created by qiuhong on 03/04/2018.
 */

public class AlertProxy {

    public static void insert(DbManager db, String type, Date time, boolean deleted) {
        AlertBean alert = new AlertBean();
        alert.setType(type);
        alert.setTime(time);
        alert.setDeleted(deleted);

        try {
            db.saveBindingId(alert);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void insert(DbManager db, AlertBean alert) {
        try {
            db.saveBindingId(alert);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
