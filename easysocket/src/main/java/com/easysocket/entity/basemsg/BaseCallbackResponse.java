package com.easysocket.entity.basemsg;

/**
 * Author：Alex
 * Date：2019/12/7
 * Note：带有标识signer的回调消息的基类
 */
public abstract class BaseCallbackResponse extends BaseResponse{
    /**
     * 客户端发给服务器的请求唯一标识，服务器反馈的时候将携带这个标识，作为识别请求对应的response
     */
    protected String singer;


    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }
}
