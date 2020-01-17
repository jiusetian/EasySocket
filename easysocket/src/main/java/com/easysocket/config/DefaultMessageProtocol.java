package com.easysocket.config;

import com.easysocket.interfaces.io.IMessageProtocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：读取io数据时，默认的消息数据格式
 */
public class DefaultMessageProtocol implements IMessageProtocol {
    @Override
    public int getHeaderLength() {
        return 4; //包头的长度，用来保存body的长度值
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length < getHeaderLength()) {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(byteOrder);
        return bb.getInt(); //body的长度以int的形式写在header那里
    }
}
