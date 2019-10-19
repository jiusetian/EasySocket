package com.easysocket;

import com.easysocket.entity.sender.SuperCallbackSender;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：继承SuperCallbackSender代表这是一个有回调的消息
 */
public class MyCallbackSender extends SuperCallbackSender {

    private String msgId;
    private String from;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
