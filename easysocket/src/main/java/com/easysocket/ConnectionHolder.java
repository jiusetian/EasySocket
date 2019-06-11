package com.easysocket;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.connect.SuperConnection;
import com.easysocket.connection.connect.TcpConnection;
import com.easysocket.entity.HostInfo;
import com.easysocket.interfaces.config.IConnectionSwitchListener;
import com.easysocket.interfaces.conn.IConnectionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：连接管理器的holder
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
     * 移除某个连接缓存
     * @param hostInfo
     */
    public void removeConnection(HostInfo hostInfo){
        mConnectionManagerMap.remove(createKey(hostInfo));
    }

    /**
     * 获取指定host的连接器，option为默认
     * @param info
     * @return
     */
    public IConnectionManager getConnection(HostInfo info) {

        IConnectionManager manager = mConnectionManagerMap.get(createKey(info));
        if (manager == null) {
            return getConnection(info, EasySocketOptions.getDefaultOptions());
        } else {
            return getConnection(info, manager.getOptions());
        }
    }

    /**
     * 获取指定host和参数opiton的连接器
     * @param info
     * @param socketOptions
     * @return
     */
    public IConnectionManager getConnection(HostInfo info, EasySocketOptions socketOptions) {
        IConnectionManager manager = mConnectionManagerMap.get(createKey(info));
        if (manager != null) { //有缓存
            manager.setOptions(socketOptions);
            return manager;
        } else {
            return createNewManagerAndCache(info, socketOptions);
        }
    }

    /**
     * 创建新的连接管理器并缓存
     *
     * @param info
     * @param socketOptions
     * @return
     */
    private IConnectionManager createNewManagerAndCache(HostInfo info, EasySocketOptions socketOptions) {
        SuperConnection manager = new TcpConnection(info); //创建连接管理器
        manager.setOptions(socketOptions); //设置参数
        //连接信息切换监听
        manager.setOnConnectionSwitchListener(new IConnectionSwitchListener() {
            @Override
            public void onSwitchConnectionInfo(IConnectionManager manager, HostInfo oldInfo,
                                               HostInfo newInfo) {
                synchronized (mConnectionManagerMap) {
                    mConnectionManagerMap.remove(createKey(oldInfo));
                    mConnectionManagerMap.put(createKey(newInfo), manager);
                }
            }
        });
        synchronized (mConnectionManagerMap) {
            mConnectionManagerMap.put(createKey(info), manager);
        }
        return manager;
    }

    /**
     * 生成保存连接的map的对应key值
     * @param hostInfo
     * @return
     */
    private String createKey(HostInfo hostInfo){
        return hostInfo.getIp()+":"+hostInfo.getPort();
    }


}
