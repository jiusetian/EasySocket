package com.easysocket.connection.action;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：连接状态
 */
public interface SocketStatus {
    int SOCKET_DISCONNECTED = 0;
    int SOCKET_CONNECTING = 1;
    int SOCKET_CONNECTED = 2;
    int SOCKET_DISCONNECTING =3;
}
