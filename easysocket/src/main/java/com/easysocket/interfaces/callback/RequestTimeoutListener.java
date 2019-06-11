package com.easysocket.interfaces.callback;

/**
 * Author：Alex
 * Date：2019/6/3
 * Note：请求超时监听接口
 */
public interface RequestTimeoutListener {
    /**
     * 请求超时回调
     * @param sign 代表标识为sign的请求超时了
     */
    void onRequstTimeout(String sign);

}
