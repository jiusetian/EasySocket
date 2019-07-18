package com.easysocket.interfaces.conn;

import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.IsReconnect;
import com.easysocket.entity.OriginReadData;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：socket行为的抽象类，实现此类可以选择性地重写对应的方法
 */
public abstract class SocketActionListener implements ISocketActionListener{
    /**
     * socket连接成功
     * @param socketAddress
     */
    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {

    }
    /**
     * socket连接失败
     * @param socketAddress
     * @param isReconnect 是否需要重连
     */
    @Override
    public void onSocketConnFail(SocketAddress socketAddress, IsReconnect isReconnect) {

    }
    /**
     * 断开socket连接
     * @param socketAddress
     * @param isReconnect 是否需要重连
     */
    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, IsReconnect isReconnect) {

    }
    /**
     * socket读数据反馈
     * @param socketAddress
     * @param originReadData
     */
    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {

    }
}
