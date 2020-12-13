package com.socker_server;

import com.socker_server.entity.IMessageProtocol;

/**
 * Author：Mapogo
 * Date：2020/9/6
 * Note：服务端的相关配置信息
 */
public class ServerConfig {

    // 消息协议
    private IMessageProtocol messageProtocol;
    // 单例
    private static ServerConfig instance = new ServerConfig();

    public static ServerConfig getInstance() {
        return instance;
    }

    private ServerConfig() {
    }

    public IMessageProtocol getMessageProtocol() {
        return messageProtocol;
    }

    public void setMessageProtocol(IMessageProtocol messageProtocol) {
        this.messageProtocol = messageProtocol;
    }
}
