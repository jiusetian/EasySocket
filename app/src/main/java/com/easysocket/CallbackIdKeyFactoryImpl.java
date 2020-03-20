package com.easysocket;

import com.easysocket.config.CallbakcIdKeyFactory;

/**
 * Author：枪花
 * Date：2020/3/20
 * Note：返回callbackID对应的key值
 */
public class CallbackIdKeyFactoryImpl extends CallbakcIdKeyFactory {

    @Override
    public String getCallbackIdKey() {
        return "callbackId";
    }
}
