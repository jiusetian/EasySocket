package com.easysocket.interfaces.conn;

import com.easysocket.entity.IClientHeart;

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
    void setClientHeart(IClientHeart clientHeart);
}
