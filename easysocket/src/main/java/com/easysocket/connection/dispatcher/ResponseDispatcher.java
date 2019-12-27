package com.easysocket.connection.dispatcher;

import com.easysocket.callback.SuperCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

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
     * 保存回调实例的map,key为每个请求对象的callbackSign
     */
    private Map<String, SuperCallBack> callbacks = new HashMap<>();
    /**
     * 保存待检测的超时请求，这是一个延时队列，元素超时的时候会被取出来
     */
    private DelayQueue<timeoutItem> timeoutQueue = new DelayQueue<>();
    /**
     * 请求超时检测的线程管理器
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
        //开始超时监听线程
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
                        timeoutItem item = timeoutQueue.take();
                        if (item != null) {
                            LogUtil.d("移除超时="+item.singer);
                            callbacks.remove(item.singer);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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
            timeoutExecutor=null;
        }

    }

    /**
     * socket行为监听，重写反馈消息回调方法即可
     */
    private SocketActionListener socketActionListener = new SocketActionListener() {
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            if (callbacks.size() == 0) return; //没有回调
            String sign = socketOptions.getCallbackSingerFactory().getCallbackSinger(originReadData);
            //获取对应的callback
            SuperCallBack callBack = callbacks.get(sign);
            if (callBack != null) {
                //回调
                callBack.onSuccess(originReadData.getBodyString());
                callbacks.remove(sign); //移除完成任务的callback
            }
        }

    };

    /**
     * 添加回调实例
     *
     * @param superCallBack
     */
    public void addSocketCallback(SuperCallBack superCallBack) {
        callbacks.put(superCallBack.getSinger(), superCallBack);
        //放入延时队列
        long time = socketOptions == null ?
                EasySocketOptions.getDefaultOptions().getRequestTimeout() : socketOptions.getRequestTimeout();
        timeoutQueue.add(new timeoutItem(superCallBack.singer, time, TimeUnit.MILLISECONDS));
    }

    /**
     * 请求延时队列的item
     */
    class timeoutItem implements Delayed {

        String singer; //当前callback的singer
        long time; //触发时间

        public timeoutItem(String singer, long time, TimeUnit timeUnit) {
            this.singer = singer;
            this.time = System.currentTimeMillis() + (time > 0 ? timeUnit.toMillis(time) : 0);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }
    }

}
