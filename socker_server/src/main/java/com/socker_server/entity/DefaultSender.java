package com.socker_server.entity;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：
 */
public class DefaultSender implements Serializable {
    private String content;

    public DefaultSender(String content){
        this.content=content;
    }

    public byte[] parse(){
        byte[] body = content.getBytes(Charset.defaultCharset());
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
