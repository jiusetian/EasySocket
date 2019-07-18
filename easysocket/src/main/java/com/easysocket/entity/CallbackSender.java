package com.easysocket.entity;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：socket请求抽象类，请求消息必须有一个ack，作为服务器对应反馈消息的识别标识
 * ack的值一般在发送请求时自动生成一个20位字符串，不用自己设置
 */
public abstract class CallbackSender implements ISender{
    /**
     * 客户端发给服务器的请求唯一标识，服务器反馈的时候将携带这个标识，作为识别请求对应的response
     */
    private String ack;

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public CallbackSender(){
    }
}
