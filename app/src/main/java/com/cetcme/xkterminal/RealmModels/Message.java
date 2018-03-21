package com.cetcme.xkterminal.RealmModels;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class Message extends RealmObject {

//    @PrimaryKey
    private String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }

    private String sender;
    private String receiver;
    private Date send_time;
    private String content;
    private boolean read;
    private boolean deleted;
    private boolean isSend;
    private boolean sendOK;

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSendOK() {
        return sendOK;
    }

    public void setSendOK(boolean sendOK) {
        this.sendOK = sendOK;
    }

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.id);
            jsonObject.put("sender", this.sender);
            jsonObject.put("receiver", this.receiver);
            jsonObject.put("send_time", this.send_time);
            jsonObject.put("content", this.content);
            jsonObject.put("read", this.read);
            jsonObject.put("deleted", this.deleted);
            jsonObject.put("isSend", this.isSend);
            jsonObject.put("sendOK", this.sendOK);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public void fromJson(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString("id");
            this.sender = jsonObject.getString("sender");
            this.receiver = jsonObject.getString("receiver");
            this.send_time = new Date(jsonObject.getString("send_time"));
            this.content = jsonObject.getString("content");
            this.read = jsonObject.getBoolean("read");
            this.deleted = jsonObject.getBoolean("deleted");
            this.isSend = jsonObject.getBoolean("isSend");
            this.sendOK = jsonObject.getBoolean("sendOK");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
