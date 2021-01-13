package com.easysocket.config;

import com.easysocket.entity.OriginReadData;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：要想实现EasySocket的回调功能，必须实现此工厂类，callbackID作为回调消息的唯一标识
 */
public abstract class CallbackIDFactory {
    /**
     * 返回callbackID
     *
     * @param
     * @return 如果没有callbackID请返回null
     */
    public abstract String getCallbackID(OriginReadData data);
}
