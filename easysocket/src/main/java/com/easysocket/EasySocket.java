package com.easysocket;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.basemsg.ISender;
import com.easysocket.entity.basemsg.SuperCallbackSender;
import com.easysocket.entity.basemsg.SuperSender;
import com.easysocket.entity.exception.InitialExeption;
import com.easysocket.entity.exception.NoNullException;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionListener;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：EasySocket api
 */
public class EasySocket {

    /**
     * 连接管理器
     */
    private static ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

    private volatile static EasySocket singleton = null; //加了volatile更加安全
    /**
     * 连接参数
     */
    private EasySocketOptions options;
    /**
     * 连接器
     */
    private IConnectionManager connection;

    /**
     * 单例
     *
     * @return
     */
    public static EasySocket getInstance() {
        if (singleton == null) {
            synchronized (EasySocket.class) {
                if (singleton == null) {
                    singleton = new EasySocket();
                }
            }
        }
        return singleton;
    }

    /**
     * 设置连接参数
     */
    public EasySocket options(EasySocketOptions socketOptions) {
        options = socketOptions;
        return this;
    }

    /**
     * 获取配置参数
     *
     * @return
     */
    public EasySocketOptions getOptions() {
        return options == null ? EasySocketOptions.getDefaultOptions() : options;
    }

    /**
     * 建立一个socket连接
     *
     * @return
     */
    public EasySocket buildConnection() {
        SocketAddress socketAddress = options.getSocketAddress();
        if (options.getSocketAddress() == null) {
            throw new InitialExeption("请在EasySocketOptions中设置SocketAddress");
        }
        //如果有备用主机则设置
        if (options.getBackupAddress() != null) {
            socketAddress.setBackupAddress(options.getBackupAddress());
        }
        connection = connectionHolder.getConnection(socketAddress,
                options == null ? EasySocketOptions.getDefaultOptions() : options);
        connection.connect(); //进行连接
        return this;
    }

    /**
     * 销毁连接
     *
     * @return
     */
    public EasySocket destroyConnection() {
        getConnection().disconnect(new Boolean(false));
        return this;
    }

    /**
     * 发送一个对象
     *
     * @param sender
     */
    public IConnectionManager upObject(ISender sender) {
        getConnection().upObject(sender);
        return connection;
    }

    /**
     * 发送一个有回调的消息
     *
     * @param sender
     * @return
     */
    public IConnectionManager upCallbackMessage(SuperCallbackSender sender) {
        getConnection().upCallbackMessage(sender);
        return connection;
    }

    /**
     * 发送String
     *
     * @param str
     */
    public IConnectionManager upString(String str) {
        getConnection().upString(str);
        return connection;
    }

    /**
     * 发送byte[]
     *
     * @param bytes
     * @return
     */
    public IConnectionManager upBytes(byte[] bytes) {
        getConnection().upBytes(bytes);
        return connection;
    }

    /**
     * 注册监听socket行为
     *
     * @param socketActionListener
     */
    public EasySocket subscribeSocketAction(ISocketActionListener socketActionListener) {
        getConnection().subscribeSocketAction(socketActionListener);
        return this;
    }

    /**
     * 开启心跳管理器
     *
     * @param clientHeart
     * @return
     */
    public EasySocket startHeartBeat(SuperSender clientHeart, HeartManager.HeartbeatListener listener) {
        getConnection().getHeartManager().startHeartbeat(clientHeart, listener);
        return this;
    }


    /**
     * 获取连接
     *
     * @return
     */
    public IConnectionManager getConnection() {
        if (connection == null) {
            throw new NoNullException("请先创建socket连接");
        }
        return connection;
    }

    /**
     * 创建指定的socket连接
     *
     * @param socketAddress
     * @param socketOptions
     * @return
     */
    public IConnectionManager buildSpecifyConnection(SocketAddress socketAddress, EasySocketOptions socketOptions) {
        IConnectionManager connectionManager = connectionHolder.getConnection(socketAddress, socketOptions == null
                ? EasySocketOptions.getDefaultOptions() : socketOptions);
        connectionManager.connect();
        return connectionManager;
    }

    /**
     * 获取指定的连接
     *
     * @param socketAddress
     * @return
     */
    public IConnectionManager getSpecifyConnection(SocketAddress socketAddress) {
        return connectionHolder.getConnection(socketAddress);
    }

    /**
     * 发送消息至指定的连接
     *
     * @param sender
     * @param socketAddress
     */
    public IConnectionManager upToSpecifyConnection(SuperSender sender, SocketAddress socketAddress) {
        IConnectionManager connect = getSpecifyConnection(socketAddress);
        if (connect != null) {
            connect.upObject(sender);
        }
        return connect;
    }

}
