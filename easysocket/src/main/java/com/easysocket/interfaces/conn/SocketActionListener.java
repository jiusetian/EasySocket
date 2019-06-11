package com.easysocket.interfaces.conn;

import com.easysocket.entity.HostInfo;
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
     * @param hostInfo
     */
    @Override
    public void onSocketConnSuccess(HostInfo hostInfo) {

    }
    /**
     * socket连接失败
     * @param hostInfo
     * @param isReconnect 是否需要重连
     */
    @Override
    public void onSocketConnFail(HostInfo hostInfo, IsReconnect isReconnect) {

    }
    /**
     * 断开socket连接
     * @param hostInfo
     * @param isReconnect 是否需要重连
     */
    @Override
    public void onSocketDisconnect(HostInfo hostInfo, IsReconnect isReconnect) {

    }
    /**
     * socket读数据反馈
     * @param hostInfo
     * @param originReadData
     */
    @Override
    public void onSocketResponse(HostInfo hostInfo, OriginReadData originReadData) {

    }
}
