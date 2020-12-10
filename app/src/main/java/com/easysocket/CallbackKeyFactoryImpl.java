package com.easysocket;

import com.easysocket.config.CallbakcKeyFactory;

/**
 * Author：枪花
 * Date：2020/3/20
 * Note：返回callbackID对应的key值
 */
public class CallbackKeyFactoryImpl extends CallbakcKeyFactory {

    @Override
    public String getCallbackKey() {
        return "callbackId";
    }
}
