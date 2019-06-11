package com.easysocket.interfaces.config;

import com.easysocket.config.EasySocketOptions;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public interface IOptions<T> {
    /**
     * 设置配置信息
     * @param socketOptions
     */
    T setOptions(EasySocketOptions socketOptions);

    /**
     * 获取配置信息
     * @return
     */
    EasySocketOptions getOptions();
}
