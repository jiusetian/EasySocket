package com.easysocket.connection.dispatcher;

import com.easysocket.callback.SuperCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.exception.NotNullException;
import com.easysocket.interfaces.callback.RequestTimeoutListener;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：反馈消息分发器
 */
public class ResponseDispatcher implements RequestTimeoutListener {
    /**
     * 保存回调实例的map,key为每个请求对象的callbackSign
     */
    private Map<String, SuperCallBack> callbacks = new HashMap<>();

    /**
     * 心跳包缓存的大小
     */
    private static final int HEART_CALLBACK_HOLDER_SIZE = 3;

    /**
     * 连接管理
     */
    IConnectionManager connectionManager;

    private EasySocketOptions socketOptions;


    public ResponseDispatcher(IConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        socketOptions=connectionManager.getOptions();
        //注册监听
        connectionManager.subscribeSocketAction(socketActionListener);
    }

    /**
     * 设置socketoptions
     * @param socketOptions
     */
    public void setSocketOptions(EasySocketOptions socketOptions){
        this.socketOptions=socketOptions;
    }

    /**
     * socket行为监听，重写反馈消息回调方法即可
     */
    private SocketActionListener socketActionListener = new SocketActionListener() {
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            if (!isEnableCallback()) return;
            String sign = socketOptions.getCallbackSingerFactory().getCallbackSinger(originReadData);
            //获取对应的callback
            SuperCallBack callBack = callbacks.get(sign);
            if (callBack == null) {
                //LogUtil.d("没有回调函数");
                return;
            }
            //回调
            callBack.onSuccess(originReadData.getBodyString());
            callbacks.remove(sign); //移除完成任务的callback
        }

    };

    //response是否为回调消息
    private boolean isEnableCallback() {
        //是否开启消息的分发
        if (!socketOptions.isEnableCallback()) return false;
        //如果要实现消息的回调功能，则需要定义如何从回调消息中去获取回调标识singer
        if (socketOptions.getCallbackSingerFactory() == null) {
            LogUtil.e(new NotNullException("CallbackSingerFactory不能为null，请根据服务器反馈消息的数据结构自定义CallbackSingerFactory"));
            return false;
        }
        return true;
    }


    /**
     * 添加回调实例
     *
     * @param superCallBack
     */
    public void addSocketCallback(SuperCallBack superCallBack) {
        superCallBack.setTimeoutListener(this);//设置callback超时的监听
        callbacks.put(superCallBack.getSinger(), superCallBack);
    }


    /**
     * @param sign 某个请求超时了
     */
    @Override
    public void onRequstTimeout(String sign) {
        LogUtil.d(sign + "请求超时了");
        callbacks.remove(sign);
    }
}
