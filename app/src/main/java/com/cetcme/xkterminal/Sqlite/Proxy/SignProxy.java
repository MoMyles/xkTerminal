package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.SignBean;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.Date;
import java.util.List;

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


    /**
     * 获取打卡数量
     * @param db
     * @return
     */
    public static long getCount(DbManager db) {
        long count = 0;
        try {
            count = db.selector(SignBean.class)
                    .where("deleted","=", false)
                    .count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }


    /**
     * 分页查询打卡
     * @param db
     * @param perPage
     * @param page
     * @return
     */
    public static List<SignBean> getByPage(DbManager db, int perPage, int page) {
        List<SignBean> list = null;
        try {
            list = db.selector(SignBean.class)
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
