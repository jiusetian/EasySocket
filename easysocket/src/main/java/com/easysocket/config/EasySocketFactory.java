package com.easysocket.config;

import com.easysocket.entity.HostInfo;

import java.net.Socket;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：socket工厂
 */
public abstract class EasySocketFactory {
    public abstract Socket createSocket(HostInfo info, EasySocketOptions options) throws Exception;
}
