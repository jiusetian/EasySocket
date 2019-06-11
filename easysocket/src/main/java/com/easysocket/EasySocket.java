package com.easysocket;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.BaseSender;
import com.easysocket.entity.HostInfo;
import com.easysocket.entity.ISender;
import com.easysocket.entity.exception.InitialExeption;
import com.easysocket.entity.exception.NoNullExeption;
import com.easysocket.interfaces.conn.IConnectionManager;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：框架对外开放的api
 */
public class EasySocket {

    //静态的连接管理器持有者
    private static ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

    private volatile static EasySocket singleton = null; //加了volatile更加安全
    /**
     * 主连接的Ip
     */
    private String mainIP = null;
    /**
     * 主连接的端口
     */
    private int mainPort = -1;
    /**
     * 主连接的参数
     */
    private EasySocketOptions mainOptions;
    /**
     * 主连接器
     */
    private IConnectionManager mainConnection;


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
    public EasySocket mainIP(String ip) {
        mainIP = ip;
        return this;
    }

    /**
     * 初始化默认的端口
     */
    public EasySocket mainPort(int port) {
        mainPort = port;
        return this;
    }

    /**
     * 设置主连接的参数
     */
    public EasySocket mainOptions(EasySocketOptions socketOptions) {
        mainOptions = socketOptions;
        return this;
    }

    /**
     * 建立一个socket主连接
     *
     * @return
     */
    public EasySocket buildMainConnection() {
        testInit(); //检查必须的初始化
        HostInfo hostInfo = new HostInfo(mainIP, mainPort);
        mainConnection = connectionHolder.getConnection(hostInfo,
                mainOptions == null ? EasySocketOptions.getDefaultOptions() : mainOptions);
        mainConnection.connect(); //进行连接
        return this;
    }


    /**
     * 向服务器发送一个对象，默认是发送给主连接
     *
     * @param sender
     */
    public IConnectionManager upObject(ISender sender) {
        getMainConnection().upObject(sender);
        return mainConnection;
    }

    /**
     * 发送一个string，默认是发送给主连接
     *
     * @param str
     */
    public IConnectionManager upString(String str) {
        getMainConnection().upString(str);
        return mainConnection;
    }

    /**
     * 发送一个byte数组
     *
     * @param bytes
     * @return
     */
    public IConnectionManager upBytes(byte[] bytes) {
        getMainConnection().upBytes(bytes);
        return mainConnection;
    }

    /**
     * 发送至主连接
     *
     * @param
     */
    private void upToMainConnection(byte[] Sender) {
        if (mainConnection == null) {
            testInit();
            throw new NoNullExeption("请先创建一个socket主连接");
        }
        mainConnection.upBytes(Sender);
    }

    /**
     * 获取主连接
     * @return
     */
    public IConnectionManager getMainConnection(){
        if (mainConnection == null) {
            testInit();
            throw new NoNullExeption("请先创建一个socket主连接");
        }
        return mainConnection;
    }

    /**
     * 创建指定的socket连接
     *
     * @param hostInfo
     * @param socketOptions
     * @return
     */
    public IConnectionManager buildSpecifyConnection(HostInfo hostInfo, EasySocketOptions socketOptions) {
        IConnectionManager connectionManager = connectionHolder.getConnection(hostInfo, socketOptions == null
                ? EasySocketOptions.getDefaultOptions() : socketOptions);
        connectionManager.connect();
        return connectionManager;
    }

    /**
     * 发送至指定的地址
     *
     * @param sender
     * @param hostInfo
     * @param socketOptions
     */
    public IConnectionManager upToSpecifyConnection(BaseSender sender, HostInfo hostInfo, EasySocketOptions socketOptions) {
        EasySocketOptions options = socketOptions == null ? EasySocketOptions.getDefaultOptions() : socketOptions;
        IConnectionManager connectionManager = connectionHolder.getConnection(hostInfo, options);
        if (connectionManager.isConnectViable())
            connectionManager.connect();
        connectionManager.upBytes(sender.parse());
        return connectionManager;
    }
    /**
     * 发送至指定的地址
     *
     * @param sender
     * @param hostInfo
     */
    public IConnectionManager upToSpecifyConnection(BaseSender sender, HostInfo hostInfo) {
        return upToSpecifyConnection(sender,hostInfo,null);
    }

    /**
     * 检查初始化是否完成
     */
    public void testInit() {
        if (mainIP == null) {
            throw new InitialExeption("没有初始化主连接的IP，请设置一个socket的IP，比如在application中进行初始化");
        }
        if (mainPort == -1) {
            throw new InitialExeption("没有初始化主连接的端口port，请设置一个socket的端口，比如在application中进行初始化");
        }
    }
}
