package com.easysocket.config;

import com.easysocket.entity.OriginReadData;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：从反馈中获取请求signer的工厂
 */
public abstract class SignerFactory {
    public abstract String createCallbackSgin(OriginReadData originReadData);
}
