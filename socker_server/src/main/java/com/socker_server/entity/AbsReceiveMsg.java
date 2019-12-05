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
     * 回调标识
     */
    private String singer;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String backSign) {
        this.singer = backSign;
    }
}
