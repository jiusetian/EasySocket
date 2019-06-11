package com.easysocket.interfaces.conn;

import com.easysocket.entity.HostInfo;
import com.easysocket.entity.IsReconnect;
import com.easysocket.entity.OriginReadData;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：socket行为监听接口
 */
public interface ISocketActionListener {
    /**
     * socket连接成功
     * @param hostInfo
     */
    void onSocketConnSuccess(HostInfo hostInfo);

    /**
     * socket连接失败
     * @param hostInfo
     * @param isReconnect 是否需要重连
     */
    void onSocketConnFail(HostInfo hostInfo, IsReconnect isReconnect);

    /**
     * 断开socket连接
     * @param hostInfo
     * @param isReconnect 是否需要重连
     */
    void onSocketDisconnect(HostInfo hostInfo, IsReconnect isReconnect);

    /**
     * socket读数据反馈
     * @param hostInfo
     * @param originReadData
     */
    void onSocketResponse(HostInfo hostInfo, OriginReadData originReadData);
}
