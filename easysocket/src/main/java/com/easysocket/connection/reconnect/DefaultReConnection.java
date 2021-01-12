package com.easysocket.connection.reconnect;

import android.os.Handler;
import android.os.HandlerThread;

import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.utils.LogUtil;

/**
 * Author：Alex
 * Date：2019/5/28
 * Note：默认重连器
 */
public class DefaultReConnection extends AbsReconnection {
    /**
     * 最大连接失败次数，超过可以切换到备用的服务器地址
     */
    private static final int MAX_CONNECTION_FAILED_TIMES = 10;
    /**
     * 连接失败的次数
     */
    private int connectionFailedTimes = 0;
    /**
     * 重连间隔不能小于10秒，为了避免全部客户端socket在同一时间连接服务端，间隔时间需要上下浮动50%
     */
    private long reconnectTimeDelay = 10 * 1000;
    /**
     * 重连线程
     */
    private HandlerThread handlerThread;
    /**
     * 实现延时任务的 handler
     */
    private Handler handler;

    public DefaultReConnection() {
    }

    @Override
    public synchronized void attach(IConnectionManager iConnectionManager) {
        super.attach(iConnectionManager);
        if (reconnectTimeDelay < connectionManager.getOptions().getConnectTimeout()) {
            reconnectTimeDelay = connectionManager.getOptions().getConnectTimeout();
        }
    }

    /**
     * 重连任务
     */
    private final Runnable RcConnTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d("---> 执行重连");
            if (isDetach) {
                shutDown();
                return;
            }
            // 是否可连接的
            if (!connectionManager.isConnectViable()) {
                LogUtil.d("当前条件不允许连接");
                // 尝试再次重连
                handler.postDelayed(RcConnTask, (long) (reconnectTimeDelay * (Math.random() + 0.5)));
                return;
            }
            // 重连
            connectionManager.connect();
        }
    };

    /**
     * 进行重连
     */
    private void reconnect() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("re_conn");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }
        LogUtil.d("重连间隔时间-->" + reconnectTimeDelay * (Math.random() + 0.5));
        handler.postDelayed(RcConnTask, (long) (reconnectTimeDelay * (Math.random() + 0.5)));
    }


    // 关闭重连线程
    private void shutDown() {
        if (handlerThread != null && handlerThread.isAlive()) {
            handlerThread.quit();
            handlerThread = null;
            handler = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        // getClass返回Class类型的对象，比较它们的类型对象是否==，其实是比较它们是否为同一个Class创建的对象
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        // 连接成功关闭重连线程
        shutDown();
    }

    @Override
    public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
        // 不需要重连，则关闭重连线程
        if (!isNeedReconnect) {
            shutDown();
            return;
        }
        connectionFailedTimes++;

        // 如果大于最大连接次数并且有备用host,则轮流切换两个host
        if (connectionFailedTimes > MAX_CONNECTION_FAILED_TIMES && socketAddress.getBackupAddress() != null) {
            connectionFailedTimes = 0; // 归零
            SocketAddress backupAddress = socketAddress.getBackupAddress();
            SocketAddress nowAddress = new SocketAddress(socketAddress.getIp(), socketAddress.getPort());
            backupAddress.setBackupAddress(nowAddress);
            if (connectionManager.isConnectViable()) {
                connectionManager.switchHost(backupAddress);
                // 切换主机地址，重新连接
                reconnect();
            }
        } else {
            reconnect();
        }

    }

    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
        // 是否需要重连
        if (!isNeedReconnect) {
            shutDown();
            return;
        }
        reconnect();
    }

    @Override
    public boolean isReconning() {
        return handlerThread != null && handlerThread.isAlive();
    }

}
