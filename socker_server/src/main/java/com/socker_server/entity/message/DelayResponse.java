package com.socker_server.entity.message;

import com.socker_server.entity.message.base.SuperResponse;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：
 */
public class DelayResponse extends SuperResponse {

    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
