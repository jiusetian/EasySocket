package com.easysocket.connection.reconnect;

import com.easysocket.entity.SocketAddress;
import com.easysocket.utils.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author：Alex
 * Date：2019/5/28
 * Note：默认的重连管理器
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
     * 重连的时间不能小于10秒
     */
    private long reconnectTimeDelay = 10 * 1000;
    /**
     * 重连线程
     */
    private ScheduledExecutorService reConnExecutor;

    public DefaultReConnection() {
    }

    /**
     * 重连任务
     */
    private final Runnable RcConnTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d("执行重连");
            if (isDetach) {
                shutDown();
                return;
            }
            //是否可连接的
            if (!connectionManager.isConnectViable()) {
                shutDown();
                return;
            }
            if (reconnectTimeDelay < connectionManager.getOptions().getConnectTimeout())
                reconnectTimeDelay = connectionManager.getOptions().getConnectTimeout();
            //连接
            connectionManager.connect();
        }
    };

    /**
     * 进行重连
     */
    private void reconnect() {
        if (reConnExecutor == null || reConnExecutor.isShutdown()) {
            reConnExecutor = Executors.newSingleThreadScheduledExecutor();
            reConnExecutor.scheduleWithFixedDelay(RcConnTask, 0, reconnectTimeDelay, TimeUnit.MILLISECONDS);
        }
    }

    //关闭重连线程
    private void shutDown() {
        if (reConnExecutor != null && !reConnExecutor.isShutdown()) {
            reConnExecutor.shutdownNow();
            reConnExecutor = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        //getClass返回Class类型的对象，比较它们的类型对象是否==，其实是比较它们是否为同一个Class创建的对象
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        //连接成功则关闭重连线程
        shutDown();
    }

    @Override
    public void onSocketConnFail(SocketAddress socketAddress, Boolean isNeedReconnect) {
        //如果连接失败，但不需要重连，则关闭
        if (!isNeedReconnect.booleanValue()) {
            shutDown();
            return;
        }
        connectionFailedTimes++;
        //如果大于最大连接次数并且有备用host,则轮流切换两个host
        if (connectionFailedTimes > MAX_CONNECTION_FAILED_TIMES && socketAddress.getBackupAddress() != null) {
            connectionFailedTimes = 0; //归零
            SocketAddress backupAddress = socketAddress.getBackupAddress();
            SocketAddress bbAddress = new SocketAddress(socketAddress.getIp(), socketAddress.getPort());
            backupAddress.setBackupAddress(bbAddress);
            if (connectionManager.isConnectViable()) {
                connectionManager.switchHost(backupAddress);
                //切换主机地址，重新连接
                reconnect();
            }
        } else {
            reconnect();
        }

    }

    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, Boolean isNeedReconnect) {
        //是否需要重连
        if (!isNeedReconnect.booleanValue()) {
            shutDown();
            return;
        }
        reconnect();
    }
}
