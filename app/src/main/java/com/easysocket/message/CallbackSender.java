package com.easysocket.message;

import com.easysocket.EasySocket;
import com.easysocket.entity.basemsg.SuperCallbackSender;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：带有回调标识的发送消息
 */
public class CallbackSender extends SuperCallbackSender {

    private String msgId;
    private String from;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    @Override
    public byte[] pack() {
        byte[] body = new Gson().toJson(this).getBytes();
        // 如果没有设置消息协议，则直接发送消息
        if (EasySocket.getInstance().getOptions().getMessageProtocol() == null) {
            return body;
        }
        ByteBuffer bb = ByteBuffer.allocate(body.length + 4);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }
}
