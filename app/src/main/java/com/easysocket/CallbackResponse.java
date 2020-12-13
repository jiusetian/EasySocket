package com.easysocket;

import com.easysocket.entity.basemsg.SuperCallbackResponse;

/**
 * Author：Alex
 * Date：2019/12/7
 * Note：带有回调标识的响应消息
 */
public class CallbackResponse extends SuperCallbackResponse {

    private String from;
    /**
     * 消息ID
     */
    private String msgId;

    private String callbackId;

    @Override
    public String toString() {
        return "SingerResponse{" +
                "from='" + from + '\'' +
                ", msgId='" + msgId + '\'' +
                ", callbackId='" + callbackId + '\'' +
                '}';
    }

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
        this.callbackId = callbackId;
    }
}
