package com.easysocket.interfaces.conn;

import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.callback.ICallBack;
import com.easysocket.interfaces.config.IOptions;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author：Alex
 * Date：2019/5/29
 * Note：连接管理的接口规范
 */
public interface IConnectionManager extends ISubscribeSocketAction, IOptions<IConnectionManager>,ISend, ICallBack {
    /**
     * 开始连接
     */
    void connect();

    /**
     * 关闭连接
     * @param isNeedReconnect 是否需要重连
     */
    void disconnect(boolean isNeedReconnect);


    /**
     * 获取socket连接状态
     * @return
     */
    int getConnectionStatus();

    /**
     * 是否可连接的
     * @return
     */
    boolean isConnectViable();

    /**
     * 切换host
     * @param socketAddress
     */
    void switchHost(SocketAddress socketAddress);

    /**
     * 获取输入流
     * @return
     */
    InputStream getInputStream();

    /**
     * 获取输出流
     * @return
     */
    OutputStream getOutStream();

    /**
     * 获取心跳管理器
     * @return
     */
    IHeartManager getHeartManager();


}
