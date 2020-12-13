package com.easysocket;

import com.easysocket.entity.basemsg.SuperCallbackSender;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：带有回调标识的发送消息
 */
public class CallbackSender extends SuperCallbackSender {

    private String msgId;
    private String from;
    private String callbackId;

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
    public String getCallbackId() {
        return callbackId;
    }

    @Override
    public void setCallbackId(String callbackId) {
        this.callbackId=callbackId;
    }
}
