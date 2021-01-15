package com.easysocket.message;

import com.easysocket.EasySocket;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * Author：Mapogo
 * Date：2021/1/13
 * Note：
 */
public class AbsMessage implements IMessage {

    public AbsMessage() {
    }

    @Override
    public byte[] pack() {

        byte[] body = new Gson().toJson(this).getBytes();
        // 如果没有设置消息协议，则直接发送消息
        if (EasySocket.getInstance().getDefOptions().getMessageProtocol() == null) {
            return body;
        }
        ByteBuffer bb = ByteBuffer.allocate(body.length + 4);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
