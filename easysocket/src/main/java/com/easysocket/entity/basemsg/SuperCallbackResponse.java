package com.easysocket.entity.basemsg;

/**
 * Author：Alex
 * Date：2019/12/7
 */
public abstract class SuperCallbackResponse implements IResponse {

    public abstract String getCallbackId();

    public abstract void setCallbackId(String callbackId);

}
