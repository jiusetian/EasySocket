package com.easysocket.entity.basemsg;

import com.easysocket.utils.Util;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：带有回调标识signer的发送消息的基类
 */
public class BaseCallbackSender extends BaseSender {

    /**
     * 客户端发给服务器的请求唯一标识，服务器反馈的时候将携带这个标识，作为识别请求对应的response
     */
    private String signer;

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    /**
     * 生成回调标识singer，在消息发送前执行
     */
    public void generateSinger() {
        signer = Util.getRandomChar(20);
    }

}

