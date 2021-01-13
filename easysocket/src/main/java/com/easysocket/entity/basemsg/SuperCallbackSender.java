package com.easysocket.entity.basemsg;

import com.easysocket.utils.Utils;

/**
 * Author：Alex
 * Date：2019/10/19
 */
public abstract class SuperCallbackSender extends SuperSender {

    private String callbackId;

    public SuperCallbackSender() {
        generateCallbackId();
    }

    public String getCallbackId() {
        return callbackId;
    }

    /**
     * 根据自己的协议打包消息
     *
     * @return
     */
    public abstract byte[] pack();

    /**
     * 随机生成一个回调标识 CallbackId，在消息发送前执行，CallbackId作为消息的唯一标识一起传给服务器，服务器反馈
     * 当前消息的时候也是携带同样的CallbackId给客户端，用以识别
     */
    public void generateCallbackId() {
        callbackId= Utils.getRandomChar(20);
    }
}

