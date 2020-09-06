package com.socker_server.entity.message.base;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：基础消息
 */
public class SuperResponse implements IResponse {


    /**
     * 消息ID
     */
    private String msgId;

    private String callbackId;

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

}
