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
     * 生成回调标识CallbackId，在消息发送前执行
     */
    public String generateCallbackId() {
        return Util.getRandomChar(20);
    }
}

