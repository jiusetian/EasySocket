package com.easysocket.entity.sender;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：有回调的消息的超级父类
 */
public class SuperCallbackSender extends SuperSender {

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

}
