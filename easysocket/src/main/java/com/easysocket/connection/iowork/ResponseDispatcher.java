package com.easysocket.connection.iowork;

import com.easysocket.callback.HeartbeatCallBack;
import com.easysocket.callback.SuperCallBack;
import com.easysocket.config.AckFactory;
import com.easysocket.entity.HostInfo;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.exception.NoNullExeption;
import com.easysocket.interfaces.callback.RequestTimeoutListener;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.ELog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：反馈消息的分发器
 */
public class ResponseDispatcher implements RequestTimeoutListener {
    /**
     * 保存回调实例的map,key为每个请求对象的callbackSign
     */
    private Map<String, SuperCallBack> callbacks = new HashMap<>();
    /**
     * 心跳包回调器的缓存
     */
    private List<HeartbeatCallBack> heartCallBacksHolder = new ArrayList<>(HEART_CALLBACK_HOLDER_SIZE);
    /**
     * 心跳包缓存的大小
     */
    private static final int HEART_CALLBACK_HOLDER_SIZE = 3;

    /**
     * 连接管理
     */
    IConnectionManager connectionManager;
    /**
     * 获取反馈消息sign的工厂，要想使用反馈消息的分发器，这个值就不能为null，
     */
    private AckFactory ackFactory;


    public ResponseDispatcher(IConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        connectionManager.subscribeSocketAction(socketActionListener); //注册
        ackFactory = connectionManager.getOptions().getAckFactory();
    }

    /**
     * socket行为监听，重写反馈消息回调方法即可
     */
    private SocketActionListener socketActionListener = new SocketActionListener() {
        @Override
        public void onSocketResponse(HostInfo hostInfo, OriginReadData originReadData) {
            if (ackFactory == null) {
                throw new NoNullExeption("AckFactory不能为null，请根据服务器反馈消息的数据结构自定义AckFactory");
            }

            String sign = ackFactory.createCallbackAck(originReadData);
            //获取对应的callback
            SuperCallBack callBack = callbacks.get(sign);
            if (callBack == null) {
                return;
            }
            //回调
            callBack.onSuccess(originReadData.getBodyString());
            //如果是心跳包的回调，那么缓存
            if (callBack instanceof HeartbeatCallBack) {
                cacheHeartbeatCallback((HeartbeatCallBack) callBack);
            }
            callbacks.remove(sign); //移除完成任务的callback
        }

    };

    /**
     * 缓存心跳包的回调
     */
    private void cacheHeartbeatCallback(HeartbeatCallBack heartbeatCallBack) {
        if (heartCallBacksHolder.size() < HEART_CALLBACK_HOLDER_SIZE) { //可以缓存
            heartCallBacksHolder.add(heartbeatCallBack);
        }
    }

    /**
     * 添加回调实例
     *
     * @param superCallBack
     */
    public void addSocketCallback(SuperCallBack superCallBack) {
        superCallBack.setTimeoutListener(this);//设置callback超时的监听
        callbacks.put(superCallBack.getAck(), superCallBack);
    }


    /**
     * 添加心跳包的callback
     *
     * @param sign
     * @param callBack
     */
    public void addHeartbeatCallBack(String sign, HeartbeatCallBack.CallBack callBack) {
        try {
            HeartbeatCallBack heartbeatCallBack;
            if (heartCallBacksHolder.size() == 0) { //没有缓存
                heartbeatCallBack = new HeartbeatCallBack();
                heartbeatCallBack.setTimeoutListener(this);
            } else { //取缓存并移除
                heartbeatCallBack = heartCallBacksHolder.get(heartCallBacksHolder.size()-1);
                heartCallBacksHolder.remove(heartCallBacksHolder.size()-1);
            }
            heartbeatCallBack.setAck(sign);
            heartbeatCallBack.setCallback(callBack);
            callbacks.put(sign, heartbeatCallBack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param sign 某个请求超时了
     */
    @Override
    public void onRequstTimeout(String sign) {
        ELog.d(sign+"请求超时了");
        //移除超时的callback
        if (callbacks.get(sign) instanceof HeartbeatCallBack) {
            cacheHeartbeatCallback((HeartbeatCallBack) callbacks.get(sign));
        }
        callbacks.remove(sign);
    }
}
