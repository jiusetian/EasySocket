package com.easysocket.connection.heartbeat;

import com.easysocket.callback.HeartbeatCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.HostInfo;
import com.easysocket.entity.IClientHeart;
import com.easysocket.entity.IsReconnect;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.exception.NoNullExeption;
import com.easysocket.interfaces.config.IOptions;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.IHeartBeatManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.utils.ELog;

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
    private IClientHeart clientHeart;
    /**
     * 心跳包发送线程管理器
     */
    private ScheduledExecutorService heartExecutor;
    /**
     * 记录心跳的失联次数
     */
    private AtomicInteger loseTimes = new AtomicInteger(-1);
    /**
     * 心跳频率
     */
    private long freq;

    public HeartBeatManager(IConnectionManager iConnectionManager, ISocketActionDispatch actionDispatch) {
        this.connectionManager = iConnectionManager;
        socketOptions = iConnectionManager.getOptions();
        actionDispatch.subscribe(this); //注册监听

    }

    /**
     * 心跳发送任务
     */
    private final Runnable beatTask = new Runnable() {
        @Override
        public void run() {
            checkoutHeartbeat();
            if (connectionManager == null) return;
            //心跳丢失次数判断
            if (socketOptions.getMaxHeartbeatLoseTimes() != -1 && loseTimes.incrementAndGet() >= socketOptions.getMaxHeartbeatLoseTimes()) {
                connectionManager.disconnect(new IsReconnect(true));
                resetLoseTimes();
            } else { //发送心跳给服务器
                connectionManager.upObject(clientHeart)
                        .onHeartCallBack(clientHeart, new HeartbeatCallBack.CallBack<String>() {
                            @Override
                            public void onResponse(String s) {
                                ELog.d("心跳管理器收到心跳="+s);
                                //收到服务器心跳
                                resetLoseTimes();//重置丢失次数
                            }
                        });
            }

        }
    };

    /**
     * 检查心跳包实例是否为null
     */
    private void checkoutHeartbeat() {
        if (clientHeart == null) {
            setClientHeart(connectionManager.getOptions().getClientHeart());
            if (clientHeart == null)
                throw new NoNullExeption("clientHeart不能为null,可以在EasySocketOptions中初始化clientHeart");
        }
    }


    @Override
    public void activateHeartbeat() {
        freq = socketOptions.getHeartbeatFreq(); //心跳频率
        if (heartExecutor == null || heartExecutor.isShutdown()) {
            heartExecutor = Executors.newSingleThreadScheduledExecutor();
            heartExecutor.scheduleWithFixedDelay(beatTask, 0, freq, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stopHeartbeat() {
        shutDown();
    }

    @Override
    public void setClientHeart(IClientHeart clientHeart) {
        this.clientHeart = clientHeart;
    }

    @Override
    public void resetLoseTimes() {
        loseTimes.set(-1);
    }

    //关闭心跳线程
    private void shutDown() {
        if (heartExecutor != null && !heartExecutor.isShutdown()) {
            heartExecutor.shutdownNow();
            heartExecutor = null;
            resetLoseTimes(); //重置
        }
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
    public void onSocketConnSuccess(HostInfo hostInfo) {
        //开启心跳
        if (connectionManager.getOptions().isActiveHeart())
            activateHeartbeat();
    }

    @Override
    public void onSocketConnFail(HostInfo hostInfo, IsReconnect isReconnect) {
        stopHeartbeat();
    }

    @Override
    public void onSocketDisconnect(HostInfo hostInfo, IsReconnect isReconnect) {
        stopHeartbeat();
    }

    @Override
    public void onSocketResponse(HostInfo hostInfo, OriginReadData originReadData) {

    }

}
