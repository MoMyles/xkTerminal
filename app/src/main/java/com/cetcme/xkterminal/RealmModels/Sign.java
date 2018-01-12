package com.cetcme.xkterminal.RealmModels;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class Sign extends RealmObject {

    private String idCard;
    private String name;
    private Date time;

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
