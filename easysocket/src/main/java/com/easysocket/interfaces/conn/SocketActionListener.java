package com.easysocket.interfaces.conn;

import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：socket行为监听的抽象类，继承此类可以选择性地重写方法
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
     * @param isNeedReconnect 是否需要重连
     */
    @Override
    public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {

    }
    /**
     * 断开socket连接
     * @param socketAddress
     * @param isNeedReconnect 是否需要重连
     */
    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {

    }
    /**
     * socket读数据反馈
     * @param socketAddress
     * @param originReadData
     */
    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {

    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, byte[] readData) {

    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, String readData) {

    }
}
