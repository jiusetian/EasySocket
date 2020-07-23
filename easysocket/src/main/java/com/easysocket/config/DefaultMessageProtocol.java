package com.easysocket.config;

import com.easysocket.EasySocket;
import com.easysocket.entity.basemsg.ISender;
import com.easysocket.interfaces.io.IMessageProtocol;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：读取io数据时，默认的消息数据格式
 */
public class DefaultMessageProtocol implements IMessageProtocol {
    @Override
    public int getHeaderLength() {
        return 4; // 包头的长度，用来保存body的长度值
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length < getHeaderLength()) {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(byteOrder);
        return bb.getInt(); // body的长度以int的形式写在header那里
    }

    @Override
    public byte[] pack(ISender sender) {
        // 默认为utf-8 Charset.forName(EasySocket.getInstance().getOptions().getCharsetName())
        byte[] body = new Gson().toJson(sender).getBytes(Charset.forName(EasySocket.getInstance().getOptions().getCharsetName()));
        // 消息头的长度，指多少个byte
        int headerLength = getHeaderLength();
        ByteBuffer bb = ByteBuffer.allocate(headerLength + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length); // header，保存body的length
        bb.put(body); // body
        return bb.array();
    }
}
