package com.socker_server.entity.message.base;

import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

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
    private String callbackId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbakcId) {
        this.callbackId = callbakcId;
    }

    @Override
    public byte[] parse() {
        //默认为utf-8 Charset.forName("UTF-8")
        byte[] body = new Gson().toJson(this).getBytes(Charset.forName("UTF-8"));
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length); //header，保存body的length
        bb.put(body); //body
        return bb.array();
    }
}
