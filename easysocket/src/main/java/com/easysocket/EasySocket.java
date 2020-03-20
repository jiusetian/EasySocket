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

    //连接管理器
    private static ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

    private volatile static EasySocket singleton = null; //加了volatile更加安全
    /**
     * 连接的Ip
     */
    private String ip = null;
    /**
     * 连接的端口
     */
    private int port;
    /**
     * 连接的参数
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
     * 初始化默认的IP
     *
     * @param ip
     */
    public EasySocket ip(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * 初始化默认的端口
     */
    public EasySocket port(int port) {
        this.port = port;
        return this;
    }

    /**
     * 设置连接的参数
     */
    public EasySocket options(EasySocketOptions socketOptions) {
        options = socketOptions;
        return this;
    }

    /**
     * 获取配置参数
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
        testInit(); //检查必须的初始化
        SocketAddress socketAddress = new SocketAddress(ip, port);
        connection = connectionHolder.getConnection(socketAddress,
                options == null ? EasySocketOptions.getDefaultOptions() : options);
        connection.connect(); //进行连接

        return this;
    }

    /**
     * 销毁socket连接
     * @return
     */
    public EasySocket destroyConnection(){
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
     * @param sender
     * @return
     */
    public IConnectionManager upCallbackMessage(SuperCallbackSender sender){
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
        getConnection().getHeartManager().startHeartbeat(clientHeart,listener);
        return this;
    }


    /**
     * 获取连接
     *
     * @return
     */
    public IConnectionManager getConnection() {
        if (connection == null) {
            testInit();
            throw new NoNullException("请先创建一个socket主连接");
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
     * 发送至指定的地址
     *
     * @param sender
     * @param socketAddress
     * @param socketOptions
     */
    public IConnectionManager upToSpecifyConnection(SuperCallbackSender sender, SocketAddress socketAddress, EasySocketOptions socketOptions) {
        EasySocketOptions options = socketOptions == null ? EasySocketOptions.getDefaultOptions() : socketOptions;
        IConnectionManager connectionManager = connectionHolder.getConnection(socketAddress, options);
        if (connectionManager.isConnectViable())
            connectionManager.connect();
        connectionManager.upBytes(sender.parse());
        return connectionManager;
    }

    /**
     * 发送至指定的地址
     *
     * @param sender
     * @param socketAddress
     */
    public IConnectionManager upToSpecifyConnection(SuperCallbackSender sender, SocketAddress socketAddress) {
        return upToSpecifyConnection(sender, socketAddress, null);
    }

    /**
     * 检查初始化是否完成
     */
    public void testInit() {
        if (ip == null) {
            throw new InitialExeption("请设置IP");
        }
    }
}
