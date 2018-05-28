package com.cetcme.xkterminal.Sqlite.Proxy;

import com.cetcme.xkterminal.Sqlite.Bean.FriendBean;
import com.cetcme.xkterminal.Sqlite.Bean.GroupBean;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

public class GroupProxy {

    public static void insert(DbManager db, String name, int number) {
        GroupBean groupBean = new GroupBean();
        groupBean.setName(name);
        groupBean.setNumber(number);

        try {
            db.saveBindingId(groupBean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void insert(DbManager db, GroupBean group) {
        try {
            db.saveBindingId(group);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询全部分组
     * @param db
     * @return
     */
    public static List<GroupBean> getAll(DbManager db) {
        List<GroupBean> list = null;
        try {
            list = db.selector(GroupBean.class)
                    .findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 删除分组
     * @param db
     * @return
     */
    public static void deleteById(DbManager db, int id) {
        try {
            db.deleteById(GroupBean.class, id);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断在不在分组内
     * @param db
     * @param number
     * @return
     */
    public static boolean hasGroup(DbManager db, int number) {
        try {
            long count = db.selector(GroupBean.class).where("number", "=", number).count();
            return count != 0;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }
}
