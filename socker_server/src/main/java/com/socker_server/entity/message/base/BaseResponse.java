package com.socker_server.entity.message.base;

import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Author：枪花
 * Date：2020/3/20
 * Note：
 */
public class BaseResponse implements IResponse {
    /**
     * 消息ID
     */
    private String msgId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public final byte[] parse() {
        //默认为utf-8
        byte[] body = new Gson().toJson(this).getBytes(Charset.forName("utf-8"));
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length); //header，保存body的length
        bb.put(body); //body
        return bb.array();
    }
}
