package com.easysocket.connection.reconnect;

import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.OriginReadData;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.IReconnListener;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：抽象重连管理器
 */
public abstract class AbsReconnection implements ISocketActionListener, IReconnListener {
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
        connectionManager.subscribeSocketAction(this); //监听socket行为
    }

    @Override
    public synchronized void detach() {
        isDetach = true;
        if (connectionManager != null)
            connectionManager.unSubscribeSocketAction(this);
    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
        //donothing
    }
}
