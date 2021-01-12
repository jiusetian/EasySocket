package com.easysocket.connection.reconnect;

import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.IReconnListener;
import com.easysocket.interfaces.conn.SocketActionListener;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：抽象重连器
 */
public abstract class AbsReconnection extends SocketActionListener implements IReconnListener {
    /**
     * 连接管理器
     */
    protected IConnectionManager connectionManager;
    /**
     * socket连接管理器是否已销毁
     */
    protected boolean isDetach;


    @Override
    public synchronized void attach(IConnectionManager iConnectionManager) {
        if (!isDetach) {
            detach();
        }
        isDetach = false;
        connectionManager = iConnectionManager;
        connectionManager.subscribeSocketAction(this); // 监听socket行为
    }

    @Override
    public synchronized void detach() {
        isDetach = true;
        if (connectionManager != null)
            connectionManager.unSubscribeSocketAction(this);
    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
        // donothing
    }

    /**
     * 是否正在重连
     * @return
     */
    public abstract boolean isReconning();
}
