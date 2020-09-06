package com.socker_server.entity.message.base;

/**
 * Author：枪花
 * Date：2020/3/20
 * Note：
 */
public class BaseResponse implements IResponse {
    /**
     * 消息ID
     */
    private String msgId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

}
