package com.socker_server.entity.message;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：带有singer的客户端消息
 */
public class SingerClientMsg extends ClientMsg {

    /**
     * 回调标识
     */
    private String singer;

    public String getSinger() {
        return singer;
    }

    public void setSinger(String backSign) {
        this.singer = backSign;
    }
}
