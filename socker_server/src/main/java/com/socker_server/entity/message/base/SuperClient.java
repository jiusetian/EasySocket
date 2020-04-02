package com.socker_server.entity.message.base;

/**
 * Author：Alex
 * Date：2019/12/6
 * Note：
 */
public class SuperClient implements IClient {

    /**
     * 消息ID
     */
    private String msgId;
    /**
     * 回调标识
     */
    private String mcallbackId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    public String getCallbackId() {
        return mcallbackId;
    }

    public void setCallbackId(String callbakcId) {
        this.mcallbackId = callbakcId;
    }
}
