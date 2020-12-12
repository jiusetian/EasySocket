package com.easysocket.connection.action;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：连接状态
 */
public interface SocketStatus {
    // 已断开连接
    int SOCKET_DISCONNECTED = 0;
    // 正在连接
    int SOCKET_CONNECTING = 1;
    // 已连接
    int SOCKET_CONNECTED = 2;
    // 正在断开连接
    int SOCKET_DISCONNECTING =3;
}
