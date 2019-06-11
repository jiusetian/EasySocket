package com.easysocket.connection.connect;

import com.easysocket.connection.iowork.ResponseDispatcher;
import com.easysocket.callback.HeartbeatCallBack;
import com.easysocket.callback.SuperCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.action.SocketAction;
import com.easysocket.connection.action.SocketActionDispatcher;
import com.easysocket.connection.action.SocketStatus;
import com.easysocket.connection.heartbeat.HeartBeatManager;
import com.easysocket.connection.iowork.IOManager;
import com.easysocket.connection.reconnect.AbsReconnection;
import com.easysocket.entity.BaseSender;
import com.easysocket.entity.HostInfo;
import com.easysocket.entity.IClientHeart;
import com.easysocket.entity.ISender;
import com.easysocket.entity.IsReconnect;
import com.easysocket.entity.exception.NoNullExeption;
import com.easysocket.interfaces.config.IConnectionSwitchListener;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.utils.ELog;
import com.easysocket.utils.EUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author：Alex
 * Date：2019/5/29
 * Note：socket连接的超类
 */
public abstract class SuperConnection implements IConnectionManager {

    /**
     * 连接状态
     */
    protected final AtomicInteger connectionStatus = new AtomicInteger(SocketStatus.SOCKET_DISCONNECTED);
    /**
     * 连接线程
     */
    private Thread connectThread; //连接线程
    /**
     * 连接信息
     */
    protected HostInfo hostInfo;
    /**
     * 连接分发器
     */
    private SocketActionDispatcher actionDispatch;
    /**
     * 重连管理器
     */
    private AbsReconnection reconnection;
    /**
     * io管理器
     */
    private IOManager ioManager;
    /**
     * 心跳管理
     */
    private HeartBeatManager heartBeatManager;
    /**
     * 配置信息
     */
    protected EasySocketOptions socketOptions;
    /**
     * socket回调分发器
     */
    private ResponseDispatcher responseDispatcher;
    /**
     * 连接切换的监听
     */
    private IConnectionSwitchListener connectionSwitchListener;

    public SuperConnection(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
        actionDispatch = new SocketActionDispatcher(this, hostInfo);
    }

    @Override
    public void subscribeSocketAction(ISocketActionListener iSocketActionListener) {
        actionDispatch.subscribe(iSocketActionListener);
    }

    @Override
    public void unSubscribeSocketAction(ISocketActionListener iSocketActionListener) {
        actionDispatch.unsubscribe(iSocketActionListener);
    }

    @Override
    public synchronized IConnectionManager setOptions(EasySocketOptions socketOptions) {
        if (socketOptions == null) return this;

        this.socketOptions = socketOptions;

        if (ioManager != null)
            ioManager.setOptions(socketOptions);

        if (heartBeatManager != null)
            heartBeatManager.setOptions(socketOptions);

        //更改了重连器
        if (reconnection != null && !reconnection.equals(socketOptions.getReconnectionManager())) {
            reconnection.detach();
            reconnection = socketOptions.getReconnectionManager();
            reconnection.attach(this);
        }
        return this;
    }

    @Override
    public EasySocketOptions getOptions() {
        return socketOptions;
    }

    @Override
    public synchronized void connect() {
        ELog.d("开始socket连接");
        //检查当前连接状态
        if (connectionStatus.get() != SocketStatus.SOCKET_DISCONNECTED) {
            return;
        }
        connectionStatus.set(SocketStatus.SOCKET_CONNECTING);
        if (hostInfo == null) {
            throw new NoNullExeption("连接参数为空，请检查是否设置了连接IP和port");
        }
        //初始化心跳管理器
        if (heartBeatManager == null)
            heartBeatManager = new HeartBeatManager(this, actionDispatch);

        //重连管理器相关
        if (reconnection != null)
            reconnection.detach();
        reconnection = socketOptions.getReconnectionManager();
        if (reconnection != null)
            reconnection.attach(this);

        //开启线程
        connectThread = new ConnectThread("connect thread for" + hostInfo);
        connectThread.setDaemon(true);
        connectThread.start();
    }

    @Override
    public synchronized void disconnect(IsReconnect isReconnect) {
        if (connectionStatus.get() == SocketStatus.SOCKET_DISCONNECTIONG) {
            return;
        }
        connectionStatus.set(SocketStatus.SOCKET_DISCONNECTIONG);

        //开启断开连接线程
        String info = hostInfo.getIp() + " : " + hostInfo.getPort();
        Thread disconnThread = new DisconnectThread(isReconnect, "disconn thread：" + info);
        disconnThread.setDaemon(true);
        disconnThread.start();
    }

    /**
     * 断开连接线程
     */
    private class DisconnectThread extends Thread {
        IsReconnect isReconnect; //是否需要重连

        public DisconnectThread(IsReconnect isReconnect, String name) {
            super(name);
            this.isReconnect = isReconnect;
        }

        @Override
        public void run() {
            try {
                //首先关闭io线程和连接线程
                if (ioManager != null)
                    ioManager.closeIO();
                if (connectThread != null && connectThread.isAlive() && !connectThread.isInterrupted()) {
                    connectThread.interrupt();
                }

                //关闭连接
                closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connectionStatus.set(SocketStatus.SOCKET_DISCONNECTED);
                actionDispatch.dispatchAction(SocketAction.ACTION_DISCONNECTION, isReconnect);
            }
        }
    }

    /**
     * 连接线程
     */
    private class ConnectThread extends Thread {

        public ConnectThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                openConnection();
            } catch (Exception e) {
                //连接异常
                e.printStackTrace();
                ELog.d("连接失败");
                connectionStatus.set(SocketStatus.SOCKET_DISCONNECTED);
                actionDispatch.dispatchAction(SocketAction.ACTION_CONN_FAIL, new IsReconnect(true)); //第二个参数是指需要重连

            }
        }
    }

    /**
     * 连接打开成功
     */
    protected void onConnectionOpened() {
        ELog.d("连接成功");
        //连接成功
        actionDispatch.dispatchAction(SocketAction.ACTION_CONN_SUCCESS);
        connectionStatus.set(SocketStatus.SOCKET_CONNECTED);
        initManager();
    }

    //初始化相关管理器
    private void initManager() {
        responseDispatcher = new ResponseDispatcher(this);
        ioManager = new IOManager(this, actionDispatch);
        ioManager.startIO();
    }

    @Override
    public synchronized void switchHost(HostInfo hostInfo) {
        if (hostInfo != null) {
            HostInfo oldHost = this.hostInfo;
            this.hostInfo = hostInfo;
            if (actionDispatch != null)
                actionDispatch.setHostInfo(hostInfo);
            if (connectionSwitchListener != null) {
                connectionSwitchListener.onSwitchConnectionInfo(this, oldHost, hostInfo);
            }
        }

    }

    public void setOnConnectionSwitchListener(IConnectionSwitchListener listener) {
        connectionSwitchListener = listener;
    }

    @Override
    public boolean isConnectViable() {
        return connectionStatus.get() == SocketStatus.SOCKET_DISCONNECTED;
    }

    @Override
    public int getConnectionStatus() {
        return connectionStatus.get();
    }

    /**
     * 打开连接
     *
     * @throws IOException
     */
    protected abstract void openConnection() throws Exception;

    /**
     * 关闭连接
     *
     * @throws IOException
     */
    protected abstract void closeConnection() throws IOException;

    /**
     * 私有的发送bytes方法
     *
     * @param buffer
     * @return
     */
    private IConnectionManager sendBuffer(byte[] buffer) {
        if (ioManager != null)
            ioManager.sendBuffer(buffer);
        return this;
    }

    @Override
    public void onCallBack(SuperCallBack callBack) {
        callBack.setSocketOptions(socketOptions);
        responseDispatcher.addSocketCallback(callBack);
    }

    @Override
    public void onHeartCallBack(IClientHeart clientHeart, HeartbeatCallBack.CallBack callBack) {
        //clientHeart.setSigner(clientHeart.getSigner());
        responseDispatcher.addHeartbeatCallBack(clientHeart.getSigner(), callBack);
    }

    @Override
    public synchronized IConnectionManager upBytes(byte[] bytes) {
        sendBuffer(bytes);
        return this;
    }

    @Override
    public synchronized IConnectionManager upString(String sender) {
        sendBuffer(sender.getBytes());
        return this;
    }

    @Override
    public synchronized IConnectionManager upObject(ISender sender) {
        //如果属于有反馈的请求，将设置一个20位随机字符串作为识别标识
        if (sender instanceof BaseSender) {
            ((BaseSender) sender).setSigner(EUtil.getRandomChar(20));
        }
        sendBuffer(sender.parse());
        return this;
    }
}
