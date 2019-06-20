package com.easysocket.config;

import com.easysocket.entity.OriginReadData;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：从反馈中获取请求ack的抽象工厂
 */
public abstract class AckFactory {
    public abstract String createCallbackAck(OriginReadData originReadData);
}
