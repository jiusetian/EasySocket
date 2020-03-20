package com.easysocket.config;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：要想实现EasySocket的回调功能，必须实现此工厂类，callbackID作为回调消息的唯一标识，而callbackIDKey是从原始的socket消息
 * 中获取callbackID的key值，比如json格式的socket信息，需要由一个key用于保存callbackID，而这个key需要用户自己定义
 */
public abstract class CallbakcIdKeyFactory {
    /**
     * 返回用于获取callbackID的key
     * @return
     */
    public abstract String getCallbackIdKey();
}
