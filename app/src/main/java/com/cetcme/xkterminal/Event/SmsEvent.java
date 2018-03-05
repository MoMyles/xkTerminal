package com.cetcme.xkterminal.Event;

import org.json.JSONObject;

/**
 * Created by qiuhong on 05/03/2018.
 */

public class SmsEvent {

    private JSONObject receiveJson;

    public SmsEvent(JSONObject receiveJson){
        this.receiveJson = receiveJson;
    }

    public JSONObject getReceiveJson() {
        return receiveJson;
    }

    public void setReceiveJson(JSONObject receiveJson) {
        this.receiveJson = receiveJson;
    }
}
