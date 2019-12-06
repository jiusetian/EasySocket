package com.socker_server.entity.message;

/**
 * Author：Alex
 * Date：2019/12/6
 * Note：服务器返回的测试消息
 */
public class ServerTestMsg extends ServerMsg{

    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
