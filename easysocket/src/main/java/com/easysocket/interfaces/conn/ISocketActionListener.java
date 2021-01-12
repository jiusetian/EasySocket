package com.easysocket.interfaces.conn;

import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：socket行为监听接口
 */
public interface ISocketActionListener {
    /**
     * socket连接成功
     * @param socketAddress
     */
    void onSocketConnSuccess(SocketAddress socketAddress);

    /**
     * socket连接失败
     * @param socketAddress
     * @param isNeedReconnect 是否需要重连
     */
    void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect);

    /**
     * 断开socket连接
     * @param socketAddress
     * @param isNeedReconnect 是否需要重连
     */
    void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect);

    /**
     * socket数据响应
     * @param socketAddress
     * @param originReadData
     */
    void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData);

    /**
     * socket数据响应
     * @param socketAddress
     * @param readData
     */
    void onSocketResponse(SocketAddress socketAddress, String readData);

    /**
     * socket数据响应
     * @param socketAddress
     * @param readData
     */
    void onSocketResponse(SocketAddress socketAddress, byte[] readData);
}
