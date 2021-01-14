package com.easysocket;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.connect.SuperConnection;
import com.easysocket.connection.connect.TcpConnection;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.config.IConnectionSwitchListener;
import com.easysocket.interfaces.conn.IConnectionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：socket连接管理器
 */
public class ConnectionHolder {

    private volatile Map<String, IConnectionManager> mConnectionManagerMap = new HashMap<>();


    private static class InstanceHolder {
        private static final ConnectionHolder INSTANCE = new ConnectionHolder();
    }

    public static ConnectionHolder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private ConnectionHolder() {
        mConnectionManagerMap.clear();
    }

    /**
     * 移除某个连接
     *
     * @param socketAddress
     */
    public void removeConnection(SocketAddress socketAddress) {
        removeConnection(createKey(socketAddress));
    }

    public void removeConnection(String socketAddress) {
        mConnectionManagerMap.remove(socketAddress);
    }

    /**
     * 获取指定SocketAddress的连接，参数配置使用默认的
     *
     * @param address
     * @return
     */
    public IConnectionManager getConnection(SocketAddress address) {
        return getConnection(createKey(address));
    }

    public IConnectionManager getConnection(String address) {
        IConnectionManager manager = mConnectionManagerMap.get(address);
        if (manager == null) {
            return getConnection(address, EasySocketOptions.getDefaultOptions());
        } else {
            return getConnection(address, manager.getOptions());
        }
    }

    /**
     * 获取指定SocketAddress的连接
     *
     * @param address
     * @param socketOptions
     * @return
     */
    public IConnectionManager getConnection(SocketAddress address, EasySocketOptions socketOptions) {
        return getConnection(createKey(address),socketOptions);
    }

    public IConnectionManager getConnection(String address, EasySocketOptions socketOptions) {
        IConnectionManager manager = mConnectionManagerMap.get(address);
        if (manager != null) { // 有缓存
            manager.setOptions(socketOptions);
            return manager;
        } else {
            return createNewManagerAndCache(address, socketOptions);
        }
    }

    /**
     * 创建新的连接并缓存
     *
     * @param address
     * @param socketOptions
     * @return
     */
    private IConnectionManager createNewManagerAndCache(SocketAddress address, EasySocketOptions socketOptions) {
        SuperConnection manager = new TcpConnection(address); // 创建连接管理器
        manager.setOptions(socketOptions); // 设置参数
        // 连接主机的切换监听
        manager.setOnConnectionSwitchListener(new IConnectionSwitchListener() {
            @Override
            public void onSwitchConnectionInfo(IConnectionManager manager, SocketAddress oldAddress,
                                               SocketAddress newAddress) {
                // 切换了另外一个主机的连接，删除旧的连接和添加新的连接
                synchronized (mConnectionManagerMap) {
                    // 首先断开连接，销毁相关线程和资源
                    mConnectionManagerMap.get(createKey(oldAddress)).disconnect(false);
                    mConnectionManagerMap.remove(createKey(oldAddress));
                    mConnectionManagerMap.put(createKey(newAddress), manager);
                }
            }
        });

        synchronized (mConnectionManagerMap) {
            mConnectionManagerMap.put(createKey(address), manager);
        }
        return manager;
    }

    private IConnectionManager createNewManagerAndCache(String address, EasySocketOptions socketOptions) {
        return createNewManagerAndCache(createSocketAddress(address), socketOptions);
    }

    /**
     * @param socketAddress
     * @return
     */
    private String createKey(SocketAddress socketAddress) {
        return socketAddress.getIp() + ":" + socketAddress.getPort();
    }

    private SocketAddress createSocketAddress(String address) {
        String[] s = address.split(":");
        return new SocketAddress(s[0], Integer.parseInt(s[1]));
    }

}
