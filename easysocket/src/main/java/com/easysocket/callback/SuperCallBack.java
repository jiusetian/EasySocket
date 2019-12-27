package com.easysocket.callback;


import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.basemsg.BaseCallbackSender;
import com.easysocket.interfaces.callback.IType;
import com.easysocket.utils.Util;

import java.lang.reflect.Type;

/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SuperCallBack<T> implements IType<T> {
    /**
     * 生成随机字符串，用来识别服务端对客户端反馈消息的唯一标识
     */
    public String singer;

    /**
     * socket参数配置
     */
    protected EasySocketOptions socketOptions;


    /**
     * @param sender
     */
    public SuperCallBack(BaseCallbackSender sender) {
        if (sender != null)
            this.singer = sender.getSinger();
    }


    public void setSocketOptions(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }


    /**
     * 获取请求的标识
     *
     * @return
     */
    public String getSinger() {
        return singer;
    }

    public void setSinger(String sign) {
        singer = sign;
    }

    public abstract void onStart();

    public abstract void onCompleted();

    public abstract void onError(Exception e);

    public abstract void onSuccess(String s);

    /**
     * 获取泛型参数的类型
     *
     * @return
     */
    @Override
    public Type getType() {
        return Util.findNeedClass(getClass());
    }

    /**
     * 获取泛型的class对象
     *
     * @return
     */
    @Override
    public Class<?> getClazz() {
        return (Class<?>) getType(); //转为泛型参数的class对象
    }
}
