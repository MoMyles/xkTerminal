package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by qiuhong on 03/04/2018.
 */

@Table(name = "t_alert")
public class AlertBean implements Serializable {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "type")
    private String type;
    @Column(name = "time")
    private Date time;
    @Column(name = "deleted")
    private boolean deleted;

    @Override
    public String toString() {
        return "AlertBean{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", deleted='" + deleted + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
