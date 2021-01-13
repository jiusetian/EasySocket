package com.easysocket.interfaces.conn;

import com.easysocket.connection.heartbeat.HeartManager;

/**
 * Author：Alex
 * Date：2019/12/8
 * Note：
 */
public interface IHeartManager {

    /**
     * 开始心跳
     * @param clientHeart
     */
    void startHeartbeat(byte[] clientHeart, HeartManager.HeartbeatListener listener);

    /**
     * 停止心跳
     */
    void stopHeartbeat();


    /**
     * 接收到心跳
     */
    void onReceiveHeartBeat();
}
