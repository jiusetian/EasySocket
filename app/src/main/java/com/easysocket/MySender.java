package com.easysocket;

import com.easysocket.entity.CallbackSender;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：
 */
public class MySender extends CallbackSender {

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

    @Override
    public byte[] parse() {
        return new byte[0];
    }
}
