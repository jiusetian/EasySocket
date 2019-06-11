package com.socker_server.entity;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：
 */
public  class AbsReceiveMsg {

    /**
     * 消息ID
     */
    private String msgId;
    /**
     * 反馈标识
     */
    private String signer;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getBackSign() {
        return signer;
    }

    public void setBackSign(String backSign) {
        this.signer = backSign;
    }
}
