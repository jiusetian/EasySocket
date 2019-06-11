package com.easysocket;

import com.google.gson.Gson;
import com.easysocket.entity.IClientHeart;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：客户端心跳
 */
public class ClientHeartBeat extends IClientHeart {
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

    @Override
    public byte[] parse() {
        byte[] body = new Gson().toJson(this).getBytes(Charset.defaultCharset());
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
