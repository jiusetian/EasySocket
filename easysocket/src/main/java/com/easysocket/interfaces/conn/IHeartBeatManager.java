package com.easysocket.interfaces.conn;

import com.easysocket.entity.sender.SuperClientHeart;

/**
 * Author：Alex
 * Date：2019/6/3
 * Note：心跳管理接口
 */
public interface IHeartBeatManager {
    /**
     * 激活心跳
     */
    void activateHeartbeat();

    /**
     * 开始心跳
     * @param clientHeart
     */
    void startHeartbeat(SuperClientHeart clientHeart);

    /**
     * 停止心跳
     */
    void stopHeartbeat();

    /**
     * 重置心跳丢失次数
     */
    void resetLoseTimes();

    /**
     * 设置客户端心跳包entity
     * @param clientHeart
     */
    void setClientHeart(SuperClientHeart clientHeart);

    /**
     * 接收到心跳
     */
    void onReceiveHeartBeat();
}
