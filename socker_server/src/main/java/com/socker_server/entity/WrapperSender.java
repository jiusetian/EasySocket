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
public class WrapperSender implements Serializable {
    private String content;

    public WrapperSender(String content){
        this.content=content;
    }

    public byte[] parse(){
        //默认为utf-8编码
        byte[] body = content.getBytes(Charset.forName("utf-8"));
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
