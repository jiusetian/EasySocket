package com.easysocket;

import android.content.Context;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.basemsg.SuperCallbackSender;
import com.easysocket.exception.InitialExeption;
import com.easysocket.exception.NotNullException;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionListener;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：EasySocket API
 */
public class EasySocket {

    /**
     * 连接的缓存
     */
    private static ConnectionHolder connectionHolder = ConnectionHolder.getInstance();
    // 单例
    private volatile static EasySocket singleton = null;
    /**
     * 默认的连接参数
     */
    private EasySocketOptions defOptions;
    /**
     * 默认的连接
     */
    private IConnectionManager defConnection;
    /**
     * 上下文
     */
    private Context context;

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
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * 获取默认的配置参数
     *
     * @return
     */
    public EasySocketOptions getDefOptions() {
        return defOptions == null ? EasySocketOptions.getDefaultOptions() : defOptions;
    }

    /**
     * 创建socket连接，此连接为默认的连接，如果你的项目只有一个Socket连接，可以用这个方法，
     * 在方法不指定连接地址的情况下，默认使用都是这个连接，
     * 比如： upMessage(byte[] message)、 connect()等
     *
     * @return
     */
    public EasySocket createConnection(EasySocketOptions options, Context context) {
        this.defOptions = options;
        this.context = context;
        SocketAddress socketAddress = options.getSocketAddress();
        if (options.getSocketAddress() == null) {
            throw new InitialExeption("请在初始化的时候设置SocketAddress");
        }
        // 如果有备用主机则设置
        if (options.getBackupAddress() != null) {
            socketAddress.setBackupAddress(options.getBackupAddress());
        }
        if (defConnection == null) {
            defConnection = connectionHolder.getConnection(socketAddress,
                    options == null ? EasySocketOptions.getDefaultOptions() : options);
        }
        // 执行连接
        defConnection.connect();
        return this;
    }

    /**
     * 连接socket，作用于默认连接
     *
     * @return
     */
    public EasySocket connect() {
        getDefconnection().connect();
        return this;
    }

    /**
     * @param address socket地址，包括ip和端口
     * @return
     */
    public EasySocket connect(String address) {
        getConnection(address).connect();
        return this;
    }


    /**
     * 关闭连接，作用于默认连接
     *
     * @param isNeedReconnect 是否需要重连
     * @return
     */
    public EasySocket disconnect(boolean isNeedReconnect) {
        getDefconnection().disconnect(isNeedReconnect);
        return this;
    }


    /**
     * 关闭连接
     *
     * @param isNeedReconnect 是否需要重连
     * @return
     */
    public EasySocket disconnect(String address, boolean isNeedReconnect) {
        getConnection(address).disconnect(isNeedReconnect);
        return this;
    }

    /**
     * 销毁连接对象，作用于默认连接
     *
     * @return
     */
    public EasySocket destroyConnection() {
        // 断开连接
        getDefconnection().disconnect(false);
        // 移除连接
        connectionHolder.removeConnection(defOptions.getSocketAddress());
        defConnection = null;
        return this;
    }


    /**
     * 销毁连接对象
     *
     * @return
     */
    public EasySocket destroyConnection(String address) {
        // 断开连接
        getConnection(address).disconnect(false);
        // 移除连接
        connectionHolder.removeConnection(address);
        return this;
    }

    /**
     * 发送有回调的消息，作用于默认连接
     *
     * @param sender
     * @return
     */
    public IConnectionManager upCallbackMessage(SuperCallbackSender sender) {
        getDefconnection().upCallbackMessage(sender);
        return defConnection;
    }

    /**
     * 发送有回调的消息
     *
     * @param sender
     * @return
     */
    public IConnectionManager upCallbackMessage(SuperCallbackSender sender, String address) {
        return getConnection(address).upCallbackMessage(sender);
    }


    /**
     * 发送byte[]
     *
     * @param message
     * @return
     */
    public IConnectionManager upMessage(byte[] message, String address) {
        return getConnection(address).upBytes(message);
    }

    /**
     * 发送byte[]，作用于默认连接
     *
     * @param message
     * @return
     */
    public IConnectionManager upMessage(byte[] message) {
        return getDefconnection().upBytes(message);
    }


    /**
     * 注册监听socket行为，作用于默认连接
     *
     * @param socketActionListener
     */
    public EasySocket subscribeSocketAction(ISocketActionListener socketActionListener) {
        getDefconnection().subscribeSocketAction(socketActionListener);
        return this;
    }


    /**
     * 注册监听socket行为
     *
     * @param socketActionListener
     */
    public EasySocket subscribeSocketAction(ISocketActionListener socketActionListener, String address) {
        getConnection(address).subscribeSocketAction(socketActionListener);
        return this;
    }

    /**
     * 开启心跳检测，作用于默认连接
     *
     * @param clientHeart
     * @return
     */
    public EasySocket startHeartBeat(byte[] clientHeart, HeartManager.HeartbeatListener listener) {
        getDefconnection().getHeartManager().startHeartbeat(clientHeart, listener);
        return this;
    }

    /**
     * 开启心跳检测
     *
     * @param clientHeart
     * @return
     */
    public EasySocket startHeartBeat(byte[] clientHeart, String address, HeartManager.HeartbeatListener listener) {
        getConnection(address).getHeartManager().startHeartbeat(clientHeart, listener);
        return this;
    }


    /**
     * 获取连接
     *
     * @return
     */
    public IConnectionManager getDefconnection() {
        if (defConnection == null) {
            throw new NotNullException("你还没有创建：" + defOptions.getSocketAddress().getIp() + ":" + defOptions.getSocketAddress().getPort()
                    + "的Socket的连接，请使用com.easysocket.EasySocket.connect()方法创建一个默认的连接");
        }
        return defConnection;
    }

    /**
     * 获取连接
     *
     * @return
     */
    public IConnectionManager getConnection(String address) {
        IConnectionManager connectionManager = connectionHolder.getConnection(address);
        if (connectionManager == null) {
            throw new NotNullException("请先创建：" + address + "的Socket连接");
        }
        return connectionManager;
    }

    /**
     * 创建指定的socket连接，如果你的项目有多个socket连接，可以用这个方法创建更多的连接，
     * 当你使用带有socket地址为参数的方法的时候，作用的就是对应的连接
     * 比如：connect(String address)、 upMessage(byte[] message, String address)等
     *
     * @param socketOptions
     * @return
     */
    public IConnectionManager createSpecifyConnection(EasySocketOptions socketOptions, Context context) {
        this.context = context;
        IConnectionManager connectionManager = connectionHolder.getConnection(socketOptions.getSocketAddress(), socketOptions == null
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
    public IConnectionManager getSpecifyConnection(String socketAddress) {
        return connectionHolder.getConnection(socketAddress);
    }

    /**
     * 发送消息至指定的连接
     *
     * @param sender
     * @param socketAddress
     */
    public IConnectionManager upToSpecifyConnection(byte[] sender, String socketAddress) {
        IConnectionManager connect = getSpecifyConnection(socketAddress);
        if (connect != null) {
            connect.upBytes(sender);
        }
        return connect;
    }

    /**
     * 是否为debug
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        EasySocketOptions.setIsDebug(debug);
    }

}
