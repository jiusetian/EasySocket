package com.easysocket.entity;

import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：默认的json格式发送内容
 *
 */
public class DefaultSender extends BaseSender {

    /**
     * 要发送的内容
     */
    private BaseSender content;

    /**
     * 通过DefaultSender可以将要发送的内容按照默认的协议解析成字节流
     *
     * @param content 要发送的内容
     */
    public DefaultSender(BaseSender content) {
        this.content = content;
    }

    @Override
    public void setAck(String ack) {
        content.setAck(ack);
    }

    @Override
    public byte[] parse() {
        byte[] body = new Gson().toJson(content).getBytes();
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
