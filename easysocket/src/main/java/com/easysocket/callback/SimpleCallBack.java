package com.easysocket.callback;


import com.easysocket.entity.basemsg.BaseCallbackSender;
import com.google.gson.Gson;

/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SimpleCallBack<T> extends SuperCallBack<T> {


    public SimpleCallBack(BaseCallbackSender sender) {
        super(sender);
        onStart();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onSuccess(String s) {
        onCompleted();
        Class<?> clazz = getClazz();
        if (clazz.equals(String.class)) { //泛型是字符串类型
            onResponse((T) s);
        } else { //非string
            Gson gson = new Gson();
            T result = (T) gson.fromJson(s, clazz);
            onResponse(result);
        }
    }

    public abstract void onResponse(T t);

}
