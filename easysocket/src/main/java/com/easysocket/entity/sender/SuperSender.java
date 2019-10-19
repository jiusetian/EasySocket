package com.easysocket.entity.sender;

import com.easysocket.EasySocket;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：消息的父类
 */
public class SuperSender implements ISender {

    @Override
    public final byte[] parse() {
        byte[] body = new Gson().toJson(this).getBytes(Charset.defaultCharset());
        final int headerLength = EasySocket.getInstance().getOptions().getReaderProtocol().getHeaderLength();
        ByteBuffer bb = ByteBuffer.allocate(headerLength + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
