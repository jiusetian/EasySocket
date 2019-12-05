package com.easysocket;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：
 */
public class ServerHeartBeat {

    private String from;
    /**
     * 消息ID
     */
    private String msgId;
    /**
     * 反馈标识
     */
    private String singer;

    @Override
    public String toString() {
        return "ServerHeartBeat{" +
                "from='" + from + '\'' +
                ", msgId='" + msgId + '\'' +
                ", singer='" + singer + '\'' +
                '}';
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getCallbackSigner() {
        return singer;
    }

    public void setCallbackSigner(String singer) {
        this.singer = singer;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
