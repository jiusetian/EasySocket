package com.socker_server.entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：读取io数据时，默认的包头数据格式
 */
public class DefaultReaderProtocol implements IReaderProtocol {
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
}
