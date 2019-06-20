package com.socker_server.entity;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：
 */
public class AbsSendMsg {

    /**
     * 消息ID
     */
    private String msgId;
    /**
     * 反馈标识
     */
    private String ack;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }
}
