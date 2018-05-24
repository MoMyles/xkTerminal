package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.AlertBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Date;
import java.util.List;

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


    /**
     * 获取报警数量
     * @param db
     * @return
     */
    public static long getCount(DbManager db) {
        long count = 0;
        try {
            count = db.selector(AlertBean.class)
                    .where("deleted","=", false)
                    .count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }


    /**
     * 分页查询报警
     * @param db
     * @param perPage
     * @param page
     * @return
     */
    public static List<AlertBean> getByPage(DbManager db, int perPage, int page) {
        List<AlertBean> list = null;
        try {
            list = db.selector(AlertBean.class)
                    .where("deleted","=",false)
                    .limit(perPage)
                    .offset(page * perPage)
                    .orderBy("time", true)
                    .findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }

}
