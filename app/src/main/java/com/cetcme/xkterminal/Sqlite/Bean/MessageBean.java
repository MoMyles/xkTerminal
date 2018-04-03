package com.cetcme.xkterminal.Sqlite.Bean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by qiuhong on 03/04/2018.
 */

@Table(name = "t_message")
public class MessageBean implements Serializable {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "sender")
    private String sender;
    @Column(name = "receiver")
    private String receiver;
    @Column(name = "content")
    private String content;
    @Column(name = "send_time")
    private Date send_time;
    @Column(name = "read")
    private boolean read;
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "isSend")
    private boolean isSend;
    @Column(name = "sendOK")
    private boolean sendOK;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSend_time() {
        return send_time;
    }

    public void setSend_time(Date send_time) {
        this.send_time = send_time;
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

    public boolean isSendOK() {
        return sendOK;
    }

    public void setSendOK(boolean sendOK) {
        this.sendOK = sendOK;
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", send_time='" + send_time + '\'' +
                ", read='" + read + '\'' +
                ", deleted='" + deleted + '\'' +
                ", isSend='" + isSend + '\'' +
                ", sendOK='" + sendOK + '\'' +
                '}';
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
}
