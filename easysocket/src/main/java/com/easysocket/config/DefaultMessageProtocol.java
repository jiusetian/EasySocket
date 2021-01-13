package com.easysocket.config;

import com.easysocket.interfaces.config.IMessageProtocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：默认的消息协议，header为4个字节，保存消息体 body的长度
 */
public class DefaultMessageProtocol implements IMessageProtocol {
    @Override
    public int getHeaderLength() {
        return 4; // 包头长度，用来保存body的长度值
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length < getHeaderLength()) {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(byteOrder);
        return bb.getInt(); // body的长度以int的形式保存在 header
    }
}
