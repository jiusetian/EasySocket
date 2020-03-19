package com.easysocket.config;

import com.easysocket.entity.OriginReadData;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：从反馈消息中获取回调标识singer的抽象工厂
 */
public abstract class GetSignerFactory {
    public abstract String getCallbackSigner(OriginReadData originReadData);
}
