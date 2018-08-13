package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "t_message_queue")
public class Message {

    @Column(name = "id", isId = true, autoGen = true)
    private int id;

    @Column(name = "message")
    private byte[] message;

    @Column(name = "send")
    private boolean send = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }
}
