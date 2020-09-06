package com.socker_server.entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：读取io数据时，默认的包头数据格式
 */
public class DefaultMessageProtocol implements IMessageProtocol {
    @Override
    public int getHeaderLength() {
        return 4;
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length < getHeaderLength()) {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(byteOrder);
        return bb.getInt(); //body的长度以int的形式写在了header那里
    }

    @Override
    public byte[] pack(byte[] body) {
        // 消息头的长度，指多少个byte
        int headerLength = getHeaderLength();
        ByteBuffer bb = ByteBuffer.allocate(headerLength + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length); // header，保存body的length
        bb.put(body); // body
        return bb.array();
    }
}
