package com.easysocket.interfaces.config;

import com.easysocket.entity.HostInfo;
import com.easysocket.interfaces.conn.IConnectionManager;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：
 */
public interface IConnectionSwitchListener {
    void onSwitchConnectionInfo(IConnectionManager manager, HostInfo oldInfo, HostInfo newInfo);
}
