package com.easysocket.callback;


import com.easysocket.interfaces.callback.IType;
import com.easysocket.utils.Util;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SuperCallBack<T> implements IType<T> {
    /**
     * 随机字符串，识别服务端反馈消息的唯一标识
     */
    private String callbackId;

    /**
     * @param callbackId 识别服务端反馈消息的唯一标识
     */
    public SuperCallBack(String callbackId) {
        this.callbackId = callbackId;
    }

    /**
     * 获取请求回调ID
     *
     * @return
     */
    public String getCallbackId() {
        return callbackId;
    }

    public abstract void onStart();

    public abstract void onCompleted();

    public abstract void onError(Exception e);

    public void onSuccess(String s) {
        onCompleted();
        Class<?> clazz = getGenericityClazz();
        if (clazz.equals(String.class)) { //泛型是String类型
            onResponse((T) s);
        } else { //非String
            Gson gson = new Gson();
            T result = (T) gson.fromJson(s, clazz);
            onResponse(result);
        }
    }

    public abstract void onResponse(T t);

    /**
     * 获取泛型参数的类型
     *
     * @return
     */
    @Override
    public Type getType() {
        return Util.findGenericityType(getClass());
    }

    /**
     * 获取泛型参数的class类型
     *
     * @return
     */
    @Override
    public Class<?> getGenericityClazz() {
        return (Class<?>) getType(); //转为泛型参数的class对象
    }
}
