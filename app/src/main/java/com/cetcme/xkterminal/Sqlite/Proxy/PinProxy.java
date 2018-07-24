package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.PinBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

public class PinProxy {

    /**
     * 获取标位数量
     * @param db
     * @return
     */
    public static long getCount(DbManager db) {
        try {
            return db.selector(PinBean.class).count();
        } catch (DbException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
