package com.easysocket.interfaces.config;

import java.nio.ByteOrder;

/**
 * 消息数据格式
 */
public interface IMessageProtocol {

    /**
     * 获取包头的长度
     */
    int getHeaderLength();

    /**
     * 获取数据包体的长度，根据协议这个长度应该写在包头中，在读取数据时用到
     */
    int getBodyLength(byte[] header, ByteOrder byteOrder);

}
