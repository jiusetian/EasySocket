package com.easysocket.entity.basemsg;

import com.easysocket.utils.Util;

/**
 * Author：Alex
 * Date：2019/10/19
 */
public abstract class SuperCallbackSender extends SuperSender {


    public SuperCallbackSender() {
        setCallbackId(generateCallbackId());
    }

    public abstract String getCallbackId();

    public abstract void setCallbackId(String callbackId);

    /**
     * 随机生成一个回调标识 CallbackId，在消息发送前执行，CallbackId作为消息的唯一标识一起传给服务器，服务器反馈
     * 当前消息的时候也是携带同样的CallbackId给客户端，用以识别
     */
    public String generateCallbackId() {
        return Util.getRandomChar(20);
    }
}

