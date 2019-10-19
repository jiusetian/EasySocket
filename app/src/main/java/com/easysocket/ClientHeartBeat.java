package com.easysocket;

import com.easysocket.entity.sender.SuperClientHeart;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：客户端心跳
 */
public class ClientHeartBeat extends SuperClientHeart {
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
    public String toString() {
        return "ClientHeartBeat{" +
                "msgId='" + msgId + '\'' +
                ", from='" + from + '\'' +
                '}';
    }

}
