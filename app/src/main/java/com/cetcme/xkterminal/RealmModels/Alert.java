package com.cetcme.xkterminal.RealmModels;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class Alert extends RealmObject {

    private String type;
    private Date time;

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
}
