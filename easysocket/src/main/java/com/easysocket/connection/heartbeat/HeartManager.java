package com.easysocket.connection.heartbeat;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.config.IOptions;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.IHeartManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.conn.SocketActionListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author：Alex
 * Date：2019/12/8
 * Note：心跳包检测管理器
 */
public class HeartManager extends SocketActionListener implements IOptions, IHeartManager {

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
    private byte[] clientHeart;
    /**
     * 心跳包发送线程
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
    /**
     * 是否激活了心跳
     */
    private boolean isActivate;


    /**
     * 心跳包接收监听
     */
    private HeartbeatListener heartbeatListener;


    public HeartManager(IConnectionManager iConnectionManager, ISocketActionDispatch actionDispatch) {
        this.connectionManager = iConnectionManager;
        socketOptions = iConnectionManager.getOptions();
        actionDispatch.subscribe(this); // 注册监听
    }

    /**
     * 心跳发送任务
     */
    private final Runnable beatTask = new Runnable() {
        @Override
        public void run() {
            // 心跳丢失次数判断，心跳包丢失了一定的次数则会进行socket的断开重连
            if (socketOptions.getMaxHeartbeatLoseTimes() != -1
                    && loseTimes.incrementAndGet() >= socketOptions.getMaxHeartbeatLoseTimes()) {
                // 断开重连
                connectionManager.disconnect(true);
                resetLoseTimes();
            } else { // 发送心跳包
                connectionManager.upBytes(clientHeart);
            }
        }
    };


    @Override
    public void startHeartbeat(byte[] clientHeart, HeartbeatListener listener) {
        this.clientHeart = clientHeart;
        this.heartbeatListener = listener;
        isActivate = true;
        openThread();
    }


    // 启动心跳线程
    private void openThread() {
        freq = socketOptions.getHeartbeatFreq(); // 心跳频率
        //  启动线程发送心跳
        if (heartExecutor == null || heartExecutor.isShutdown()) {
            heartExecutor = Executors.newSingleThreadScheduledExecutor();
            heartExecutor.scheduleWithFixedDelay(beatTask, 0, freq, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 停止心跳发送
     */
    @Override
    public void stopHeartbeat() {
        isActivate = false;
        closeThread();
    }

    // 停止心跳线程
    private void closeThread() {
        if (heartExecutor != null && !heartExecutor.isShutdown()) {
            heartExecutor.shutdownNow();
            heartExecutor = null;
            resetLoseTimes(); // 重置
        }
    }

    @Override
    public void onReceiveHeartBeat() {
        resetLoseTimes();
    }


    private void resetLoseTimes() {
        loseTimes.set(-1);
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        if (isActivate) {
            openThread();
        }
    }

    @Override
    public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
        // 如果不需要重连，则停止心跳频率线程
        if (!isNeedReconnect) {
            closeThread();
        }
    }

    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
        // 如果不需要重连，则停止心跳检测
        if (!isNeedReconnect) {
            closeThread();
        }
    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
        if (heartbeatListener != null && heartbeatListener.isServerHeartbeat(originReadData)) {
            // 收到服务器心跳
            onReceiveHeartBeat();
        }
    }

    @Override
    public Object setOptions(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
        freq = socketOptions.getHeartbeatFreq();
        freq = freq < 1000 ? 1000 : freq; // 不能小于一秒
        return this;
    }

    @Override
    public EasySocketOptions getOptions() {
        return socketOptions;
    }

    public interface HeartbeatListener {
        // 是否为服务器心跳
        boolean isServerHeartbeat(OriginReadData orginReadData);
    }

}
