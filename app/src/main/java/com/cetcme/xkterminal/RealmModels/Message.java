package com.cetcme.xkterminal.RealmModels;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class Message extends RealmObject {

    private String sender;
    private String receiver;
    private Date send_time;
    private String content;
    private boolean read;



    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getSend_time() {
        return send_time;
    }

    public void setSend_time(Date send_time) {
        this.send_time = send_time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
