package com.easysocket.callback;


import com.easysocket.entity.OriginReadData;

/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SuperCallBack {
    /**
     * 随机字符串，识别服务端应答消息的唯一标识
     */
    private String callbackId;

    /**
     * @param callbackId 识别服务端应答消息的唯一标识
     */
    public SuperCallBack(String callbackId) {
        this.callbackId = callbackId;
    }

    /**
     * 获取回调ID
     *
     * @return
     */
    public String getCallbackId() {
        return callbackId;
    }

    public abstract void onStart();

    public abstract void onCompleted();

    public abstract void onError(Exception e);

    public void onSuccess(OriginReadData data) {
        onCompleted();
        onResponse(data);
    }

    public abstract void onResponse(OriginReadData data);

}
