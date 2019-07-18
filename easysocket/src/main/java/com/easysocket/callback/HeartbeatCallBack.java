package com.easysocket.callback;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.IClientHeart;
import com.easysocket.utils.Util;
import com.google.gson.Gson;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：心跳包的反馈回调
 */
public class HeartbeatCallBack<T> extends SuperCallBack<T> {

    //心跳包回调
    private CallBack callback;

    public HeartbeatCallBack() {
        super(null);
    }

    /**
     * @param
     * @param
     */
    private HeartbeatCallBack(IClientHeart clientHeart) {
        super(clientHeart);
    }

    @Override
    public void openTimeoutTask() {
        if (socketOptions == null)
            socketOptions = EasySocketOptions.getDefaultOptions();
        handler.postDelayed(timeTask, socketOptions.getHeartbeatFreq() * socketOptions.getMaxHeartbeatLoseTimes());
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onCompleted() {
        closeTimeoutTask(); //关闭超时检测
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onSuccess(String s) {
        onCompleted();
        if (callback == null) return;
        Class<?> clazz = (Class<?>) Util.findNeedClass(callback.getClass()); //获取泛型callback泛型的class类型
        if (clazz.equals(String.class)) { //泛型是字符串类型
            callback.onResponse(s);
        } else { //非string
            Gson gson = new Gson();
            callback.onResponse(gson.fromJson(s, clazz));
        }
    }

    public void setCallback(CallBack<T> callback) {
        this.callback = callback;
        openTimeoutTask(); //打开超时监听
    }

    public abstract static class CallBack<T> {
        public abstract void onResponse(T t);
    }

}
