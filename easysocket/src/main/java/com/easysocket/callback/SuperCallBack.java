package com.easysocket.callback;


import android.os.Handler;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.BaseSender;
import com.easysocket.entity.exception.RequestTimeOutException;
import com.easysocket.interfaces.callback.IType;
import com.easysocket.interfaces.callback.RequestTimeoutListener;
import com.easysocket.utils.EUtil;

import java.lang.reflect.Type;

/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SuperCallBack<T> implements IType<T> {
    /**
     * 生成随机字符串，用来识别服务端对客户端反馈的唯一标识
     */
    public String signer;
    /**
     * 超时回调
     */
    private RequestTimeoutListener timeoutListener;
    /**
     * socket参数配置
     */
    protected EasySocketOptions socketOptions;

    /**
     * 执行请求超时的handler,handler一般设置为static，因为同一个线程必须使用同一个handler对象，一个线程不能有多个handler对象
     */
    protected static Handler handler= EUtil.getHandler(true);

    /**
     * @param sender
     */
    public SuperCallBack(BaseSender sender) {
        if (sender != null)
            this.signer = sender.getSigner();
    }

    /**
     * 设置超时监听
     *
     * @param timeoutListener
     */
    public void setTimeoutListener(RequestTimeoutListener timeoutListener) {
        this.timeoutListener = timeoutListener;
    }

    public void setSocketOptions(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }

    /**
     * 执行超时的任务
     */
    protected Runnable timeTask = new Runnable() {
        @Override
        public void run() {
            if (timeoutListener != null){
                timeoutListener.onRequstTimeout(signer); //通知
                onError(new RequestTimeOutException("请求超时了"));
            }

        }
    };


    /**
     * 打开超时任务
     */
    protected void openTimeoutTask() {
        handler.postDelayed(timeTask, socketOptions == null ?
                EasySocketOptions.getDefaultOptions().getRequestTimeout() : socketOptions.getRequestTimeout());
    }

    /**
     * 关闭超时任务
     */
    protected void closeTimeoutTask() {
        if (handler != null)
            handler.removeCallbacks(timeTask);
    }

    /**
     * 获取请求的标识
     *
     * @return
     */
    public String getSigner() {
        return signer;
    }

    public void setSigner(String sign) {
        signer = sign;
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
        return EUtil.findNeedClass(getClass());
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
