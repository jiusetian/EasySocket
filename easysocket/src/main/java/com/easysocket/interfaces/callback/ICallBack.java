package com.easysocket.interfaces.callback;

import com.easysocket.callback.HeartbeatCallBack;
import com.easysocket.callback.SuperCallBack;
import com.easysocket.entity.sender.SuperClientHeart;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：
 */
public interface ICallBack {
    /**
     * socket请求回调
     * @param callBack
     */
    void onCallBack(SuperCallBack callBack);

    /**
     * 心跳包的回调
     * @param clientHeart
     * @param callBack
     */
    void onHeartCallBack(SuperClientHeart clientHeart, HeartbeatCallBack.CallBack callBack);

}
