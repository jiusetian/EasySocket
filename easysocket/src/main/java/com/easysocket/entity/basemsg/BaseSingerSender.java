package com.easysocket.entity.basemsg;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：带有回调标识singer的发送消息的基类
 */
public class BaseSingerSender extends BaseSender {

    /**
     * 客户端发给服务器的请求唯一标识，服务器反馈的时候将携带这个标识，作为识别请求对应的response
     */
    private String singer;

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

}

