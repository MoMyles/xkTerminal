package com.cetcme.xkterminal.RealmModels;

import io.realm.RealmObject;

/**
 * Created by qiuhong on 16/03/2018.
 */

public class Friend extends RealmObject {

    private String name;
    private String number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
