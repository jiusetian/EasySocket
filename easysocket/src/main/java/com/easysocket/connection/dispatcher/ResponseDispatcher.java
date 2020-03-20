package com.easysocket.connection.dispatcher;

import com.easysocket.callback.SuperCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.basemsg.SuperCallbackSender;
import com.easysocket.entity.exception.RequestTimeOutException;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;
import com.easysocket.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：回调消息分发器
 */
public class ResponseDispatcher {
    /**
     * 保存回调监听实例,key为请求回调标识callbackId
     */
    private Map<String, SuperCallBack> callbacks = new HashMap<>();
    /**
     * 保存需要进行超时检测的请求，这是一个延时队列，元素超时的时候会被取出来
     */
    private DelayQueue<timeoutItem> timeoutQueue = new DelayQueue<>();
    /**
     * 超时检测的线程管理器
     */
    private ExecutorService timeoutExecutor;

    /**
     * 连接管理
     */
    IConnectionManager connectionManager;

    private EasySocketOptions socketOptions;


    public ResponseDispatcher(IConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        socketOptions = connectionManager.getOptions();
        //注册监听
        connectionManager.subscribeSocketAction(socketActionListener);
        //开始超时检测线程
        startTimeoutThread();
    }

    /**
     * 设置socketoptions
     *
     * @param socketOptions
     */
    public void setSocketOptions(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }

    /**
     * 超时检测线程
     */
    private void startTimeoutThread() {
        if (timeoutExecutor == null || timeoutExecutor.isShutdown()) {
            timeoutExecutor = Executors.newSingleThreadExecutor();
            timeoutExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //只有超时的元素才会被取出，没有的话会被等待
                        timeoutItem item = timeoutQueue.take();
                        if (item != null) {
                            SuperCallBack callBack = callbacks.remove(item.callbackId);
                            if (callBack != null)
                                callBack.onError(new RequestTimeOutException("request timeout"));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //继续循环
                    if (!timeoutExecutor.isShutdown()) {
                        run();
                    }
                }
            });
        }
    }

    /**
     * 关闭线程
     */
    public void stopThread() {
        if (timeoutExecutor != null && !timeoutExecutor.isShutdown()) {
            //shutdown和shutdownNow的主要区别是前者中断未执行的线程，后者中断所有线程
            timeoutExecutor.shutdownNow();
            timeoutExecutor = null;
        }

    }

    /**
     * socket行为监听，重写反馈消息的回调方法
     */
    private SocketActionListener socketActionListener = new SocketActionListener() {
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            if (callbacks.size() == 0) return;
            if (socketOptions.getCallbakcIdKeyFactory() == null) return;
            try {
                String callbackIdKey = socketOptions.getCallbakcIdKeyFactory().getCallbackIdKey();
                JSONObject data = new JSONObject(originReadData.getBodyString());
                String callbackId = data.getString(callbackIdKey);
                //获取signer对应的callback
                SuperCallBack callBack = callbacks.get(callbackId);
                if (callBack != null) {
                    //回调
                    callBack.onSuccess(originReadData.getBodyString());
                    callbacks.remove(callbackId); //移除完成任务的callback
                    LogUtil.d("移除的callbackId=" + callbackId );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    };


    /**
     * 添加回调实例
     *
     * @param superCallBack
     */
    public void addSocketCallback(SuperCallBack superCallBack) {
        callbacks.put(superCallBack.getCallbackId(), superCallBack);
        //放入延时队列
        long delayTime = socketOptions == null ?
                EasySocketOptions.getDefaultOptions().getRequestTimeout() : socketOptions.getRequestTimeout();
        timeoutQueue.add(new timeoutItem(superCallBack.getCallbackId(), delayTime, TimeUnit.MILLISECONDS));
    }

    /**
     * 延时队列的item
     */
    class timeoutItem implements Delayed {

        String callbackId; //当前callback的callbackId
        long executeTime; //触发时间

        public timeoutItem(String callbackId, long delayTime, TimeUnit timeUnit) {
            this.callbackId = callbackId;
            this.executeTime = System.currentTimeMillis() + (delayTime > 0 ? timeUnit.toMillis(delayTime) : 0);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return executeTime - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * 检查当前callbackID是否已经存在，如果已经存在一个了，那么将更新当前消息的callbackID
     *
     * @param callbackSender
     * @return
     */
    public void checkCallbackSender(SuperCallbackSender callbackSender) {

        Util.checkNotNull(socketOptions.getCallbakcIdKeyFactory(), "要想实现EasySocket的回调功能，CallbackIdFactory不能为null，" +
                "请实现一个CallbackIdFactory并在初始化的时候通过EasySocketOptions的setCallbackIdKeyFactory进行配置");
        String callbackId = callbackSender.getCallbackId();
        if (callbacks.containsKey(callbackId)) {
            callbackSender.setCallbackId(callbackSender.generateCallbackId());
        }
    }


}
