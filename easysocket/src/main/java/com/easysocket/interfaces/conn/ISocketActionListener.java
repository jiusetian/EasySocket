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
    void onSocketConnFail(SocketAddress socketAddress, Boolean isNeedReconnect);

    /**
     * 断开socket连接
     * @param socketAddress
     * @param isNeedReconnect 是否需要重连
     */
    void onSocketDisconnect(SocketAddress socketAddress, Boolean isNeedReconnect);

    /**
     * socket读数据反馈
     * @param socketAddress
     * @param originReadData
     */
    void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData);
}
