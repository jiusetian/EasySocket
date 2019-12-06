package com.easysocket.connection.heartbeat;

import android.util.LruCache;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.NeedReconnect;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.sender.SuperClientHeart;
import com.easysocket.interfaces.config.IOptions;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.IHeartBeatManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.utils.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author：Alex
 * Date：2019/5/28
 * Note：
 */
public class HeartBeatManager implements IOptions, ISocketActionListener, IHeartBeatManager {
    /**
     * 连接器
     */
    private IConnectionManager connectionManager;
    /**
     * 连接参数
     */
    private EasySocketOptions socketOptions;
    /**
     * 客户端心跳包
     */
    private SuperClientHeart clientHeart;
    /**
     * 心跳包发送线程管理器
     */
    private ScheduledExecutorService heartExecutor;
    /**
     * 记录心跳的失联次数
     */
    private AtomicInteger loseTimes = new AtomicInteger(-1);
    /**
     * 是否激活了心跳检查功能
     */
    private boolean isActityHeart;
    /**
     * 心跳频率
     */
    private long freq;
    /**
     * 保存发送心跳的回调singer，最大容量是心跳的允许做大丢失次数,如果超过这个值，LruCache自动删除最旧的那个singer，然后即使对应的心跳反馈了，也是作废的
     */
    private LruCache<String, String> lruSingers;

    public HeartBeatManager(IConnectionManager iConnectionManager, ISocketActionDispatch actionDispatch) {
        this.connectionManager = iConnectionManager;
        socketOptions = iConnectionManager.getOptions();
        lruSingers = new LruCache<>(socketOptions.getMaxHeartbeatLoseTimes());
        actionDispatch.subscribe(this); //注册监听
    }

    /**
     * 心跳发送任务
     */
    private final Runnable beatTask = new Runnable() {
        @Override
        public void run() {
            //心跳丢失次数判断
            if (socketOptions.getMaxHeartbeatLoseTimes() != -1 && loseTimes.incrementAndGet() >= socketOptions.getMaxHeartbeatLoseTimes()) {
                connectionManager.disconnect(new NeedReconnect(true));
                resetLoseTimes();
            } else { //发送心跳给服务器,如果启动的回调功能，这里会自动接收到服务器心跳
                connectionManager.upObject(clientHeart);
                //添加singer到LruCache中
                lruSingers.put(clientHeart.getSinger(), clientHeart.getSinger());
            }
        }
    };

    /**
     * 检查自动发送心跳是否可行
     */
    private boolean isEnableAutoHeart() {
        if (connectionManager == null) return false;
        if (clientHeart == null) setClientHeart(socketOptions.getClientHeart());
        if (clientHeart == null) {
            LogUtil.e("clientHeart不能为null");
            return false;
        }
        //是否开启消息的分发
        if (!socketOptions.isActiveResponseDispatch()) return false;
        //如果要实现消息的回调功能，则需要定义如何从回调消息中去获取回调标识singer
        if (socketOptions.getCallbackSingerFactory() == null) {
            LogUtil.e("CallbackSingerFactory不能为null，请根据服务器反馈消息的数据结构自定义CallbackSingerFactory");
            return false;
        }
        return true;
    }


    @Override
    public void activateHeartbeat() {
        if (!isEnableAutoHeart()) return; //是否可行自动发送心跳
        freq = socketOptions.getHeartbeatFreq(); //心跳频率
        if (heartExecutor == null || heartExecutor.isShutdown()) {
            heartExecutor = Executors.newSingleThreadScheduledExecutor();
            heartExecutor.scheduleWithFixedDelay(beatTask, 0, freq, TimeUnit.MILLISECONDS);
        }
        isActityHeart=true;
    }

    @Override
    public void stopHeartbeat() {
        if (heartExecutor != null && !heartExecutor.isShutdown()) {
            heartExecutor.shutdownNow();
            heartExecutor = null;
            resetLoseTimes(); //重置
        }
        isActityHeart=false;
    }

    @Override
    public void startHeartbeat(SuperClientHeart clientHeart) {
        setClientHeart(clientHeart);
        socketOptions.setActiveHeart(true); //修改为启动心跳管理
        socketOptions.setClientHeart(clientHeart);
        resetLoseTimes();
        activateHeartbeat();
    }

    @Override
    public void setClientHeart(SuperClientHeart clientHeart) {
        this.clientHeart = clientHeart;
    }

    @Override
    public void resetLoseTimes() {
        loseTimes.set(-1);
    }

    @Override
    public void onReceiveHeartBeat() {
        resetLoseTimes();
    }


    @Override
    public Object setOptions(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
        freq = socketOptions.getHeartbeatFreq();
        freq = freq < 1000 ? 1000 : freq; //不能小于一秒
        return this;
    }

    @Override
    public EasySocketOptions getOptions() {
        return socketOptions;
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        //开启心跳
        if (socketOptions.isActiveHeart())
            activateHeartbeat();
    }

    @Override
    public void onSocketConnFail(SocketAddress socketAddress, NeedReconnect needReconnect) {
        stopHeartbeat();
    }

    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, NeedReconnect needReconnect) {
        stopHeartbeat();
    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
        if (!isActityHeart) return;
        String singer = socketOptions.getCallbackSingerFactory().getCallbackSinger(originReadData);
        //代表是心跳消息并且还有效
        if (lruSingers.remove(singer) != null) {
            LogUtil.d("接收到自动心跳=" + originReadData.getBodyString());
            onReceiveHeartBeat();
        }
    }

}
