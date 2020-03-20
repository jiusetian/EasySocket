package com.easysocket.callback;


/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SimpleCallBack<T> extends SuperCallBack<T> {


    public SimpleCallBack(String callbackId) {
        super(callbackId);
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


}
