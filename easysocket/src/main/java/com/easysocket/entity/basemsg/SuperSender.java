package com.easysocket.entity.basemsg;

import com.easysocket.EasySocket;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：基础消息
 */
public class SuperSender implements ISender {

    @Override
    public final byte[] parse() {
        //默认为utf-8
        byte[] body = new Gson().toJson(this).getBytes(Charset.forName(EasySocket.getInstance().getOptions().getCharsetName()));
        int headerLength = EasySocket.getInstance().getOptions().getMessageProtocol().getHeaderLength();
        ByteBuffer bb = ByteBuffer.allocate(headerLength + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length); //header，保存body的length
        bb.put(body); //body
        return bb.array();
    }

}
