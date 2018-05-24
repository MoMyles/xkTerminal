package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.InoutBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Date;
import java.util.List;

public class InoutProxy {

    public static void insert(DbManager db, int type, int count, int lon, int lat, Date time) {
        InoutBean inoutBean = new InoutBean();
        inoutBean.setType(type);
        inoutBean.setCount(count);
        inoutBean.setLon(lon);
        inoutBean.setLat(lat);
        inoutBean.setTime(time);

        try {
            db.saveBindingId(inoutBean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取数量
     * @param db
     * @return
     */
    public static long getCount(DbManager db) {
        long count = 0;
        try {
            count = db.selector(InoutBean.class).count();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return count;
    }


    /**
     * 分页查询
     * @param db
     * @param perPage
     * @param page
     * @return
     */
    public static List<InoutBean> getByPage(DbManager db, int perPage, int page) {
        List<InoutBean> list = null;
        try {
            list = db.selector(InoutBean.class)
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
