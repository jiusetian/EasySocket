package com.easysocket.connection.connect;

import com.easysocket.EasySocket;
import com.easysocket.callback.SuperCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.action.SocketAction;
import com.easysocket.connection.action.SocketStatus;
import com.easysocket.connection.dispatcher.CallbackResponseDispatcher;
import com.easysocket.connection.dispatcher.SocketActionDispatcher;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.connection.iowork.IOManager;
import com.easysocket.connection.reconnect.AbsReconnection;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.basemsg.SuperCallbackSender;
import com.easysocket.exception.NotNullException;
import com.easysocket.interfaces.config.IConnectionSwitchListener;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.utils.LogUtil;
import com.easysocket.utils.Utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author：Alex
 * Date：2019/5/29
 * Note：socket连接的超类
 */
public abstract class SuperConnection implements IConnectionManager {

    /**
     * 连接状态，初始值为断开连接
     */
    protected final AtomicInteger connectionStatus = new AtomicInteger(SocketStatus.SOCKET_DISCONNECTED);
    /**
     * 连接线程
     */
    private ExecutorService connExecutor;
    /**
     * socket地址信息
     */
    protected SocketAddress socketAddress;
    /**
     * socket行为分发器
     */
    private SocketActionDispatcher actionDispatcher;
    /**
     * 重连管理器
     */
    private AbsReconnection reconnection;
    /**
     * io管理器
     */
    private IOManager ioManager;
    /**
     * 心跳管理器
     */
    private HeartManager heartManager;
    /**
     * 配置信息
     */
    protected EasySocketOptions socketOptions;
    /**
     * socket回调消息的分发器
     */
    private CallbackResponseDispatcher callbackResponseDispatcher;
    /**
     * 连接切换的监听
     */
    private IConnectionSwitchListener connectionSwitchListener;

    public SuperConnection(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        actionDispatcher = new SocketActionDispatcher(this, socketAddress);
    }

    @Override
    public void subscribeSocketAction(ISocketActionListener iSocketActionListener) {
        actionDispatcher.subscribe(iSocketActionListener);
    }

    @Override
    public void unSubscribeSocketAction(ISocketActionListener iSocketActionListener) {
        actionDispatcher.unsubscribe(iSocketActionListener);
    }

    @Override
    public synchronized IConnectionManager setOptions(EasySocketOptions socketOptions) {
        if (socketOptions == null) return this;

        this.socketOptions = socketOptions;

        if (ioManager != null)
            ioManager.setOptions(socketOptions);

        if (heartManager != null)
            heartManager.setOptions(socketOptions);

        if (callbackResponseDispatcher != null)
            callbackResponseDispatcher.setSocketOptions(socketOptions);

        // 更改了重连器
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
        LogUtil.d("---> socket开始连接");
        if (socketAddress.getIp() == null) {
            throw new NotNullException("请检查是否设置了IP地址");
        }
        // 正在连接
        connectionStatus.set(SocketStatus.SOCKET_CONNECTING);

        // 心跳管理器
        if (heartManager == null) {
            heartManager = new HeartManager(this, actionDispatcher);
        }

        // 重连管理器
        if (reconnection != null) {
            reconnection.detach();
        }
        reconnection = socketOptions.getReconnectionManager();
        if (reconnection != null) {
            reconnection.attach(this);
        }

        // 开启分发消息线程
        if (actionDispatcher != null) {
            actionDispatcher.startDispatchThread();
        }

        // 开启连接线程
        if (connExecutor == null || connExecutor.isShutdown()) {
            // 核心线程数为0，非核心线程数可以有Integer.MAX_VALUE个，存活时间为60秒，适合于在不断进行连接的情况下，避免重复创建和销毁线程
            connExecutor = Executors.newCachedThreadPool();
        }
        // 执行连接任务
        connExecutor.execute(connTask);
    }

    @Override
    public synchronized void disconnect(boolean isNeedReconnect) {
        // 判断当前socket的连接状态
        if (connectionStatus.get() == SocketStatus.SOCKET_DISCONNECTED) {
            return;
        }
        // 正在重连中
        if (isNeedReconnect && reconnection.isReconning()) {
            return;
        }
        // 正在断开连接
        connectionStatus.set(SocketStatus.SOCKET_DISCONNECTING);

        // 开启断开连接线程
        String info = socketAddress.getIp() + " : " + socketAddress.getPort();
        Thread disconnThread = new DisconnectThread(isNeedReconnect, "disconn thread：" + info);
        disconnThread.setDaemon(true);
        disconnThread.start();
    }

    /**
     * 断开连接线程
     */
    private class DisconnectThread extends Thread {
        boolean isNeedReconnect; // 当前连接的断开是否需要自动重连

        public DisconnectThread(boolean isNeedReconnect, String name) {
            super(name);
            this.isNeedReconnect = isNeedReconnect;
        }

        @Override
        public void run() {
            try {
                // 关闭io线程
                if (ioManager != null)
                    ioManager.closeIO();
                // 关闭回调分发器线程
                if (callbackResponseDispatcher != null)
                    callbackResponseDispatcher.shutdownThread();
                // 关闭连接线程
                if (connExecutor != null && !connExecutor.isShutdown()) {
                    connExecutor.shutdown();
                    connExecutor = null;
                }
                // 关闭连接
                closeConnection();
                LogUtil.d("---> 关闭socket连接");
                connectionStatus.set(SocketStatus.SOCKET_DISCONNECTED);
                actionDispatcher.dispatchAction(SocketAction.ACTION_DISCONNECTION, new Boolean(isNeedReconnect));
            } catch (IOException e) {
                // 断开连接发生异常
                e.printStackTrace();
            }
        }
    }

    // 连接任务
    private Runnable connTask = new Runnable() {
        @Override
        public void run() {
            try {
                openConnection();
            } catch (Exception e) {
                // 连接异常
                e.printStackTrace();
                LogUtil.d("---> socket连接失败");
                connectionStatus.set(SocketStatus.SOCKET_DISCONNECTED);
                // 第二个参数指需要重连
                actionDispatcher.dispatchAction(SocketAction.ACTION_CONN_FAIL, new Boolean(true));

            }
        }
    };

    /**
     * 连接成功
     */
    protected void onConnectionOpened() {
        LogUtil.d("---> socket连接成功");
        actionDispatcher.dispatchAction(SocketAction.ACTION_CONN_SUCCESS);
        connectionStatus.set(SocketStatus.SOCKET_CONNECTED);
        openSocketManager();
    }

    // 开启socket相关管理器
    private void openSocketManager() {
        if (callbackResponseDispatcher == null)
            callbackResponseDispatcher = new CallbackResponseDispatcher(this);
        if (ioManager == null) {
            ioManager = new IOManager(this, actionDispatcher);
        }
        ioManager.startIO();

        // 启动相关线程
        callbackResponseDispatcher.engineThread();
        ioManager.startIO();
    }

    // 切换了主机IP和端口
    @Override
    public synchronized void switchHost(SocketAddress socketAddress) {
        if (socketAddress != null) {
            SocketAddress oldAddress = this.socketAddress;
            this.socketAddress = socketAddress;

            if (actionDispatcher != null)
                actionDispatcher.setSocketAddress(socketAddress);
            // 切换主机
            if (connectionSwitchListener != null) {
                connectionSwitchListener.onSwitchConnectionInfo(this, oldAddress, socketAddress);
            }
        }

    }

    public void setOnConnectionSwitchListener(IConnectionSwitchListener listener) {
        connectionSwitchListener = listener;
    }

    @Override
    public boolean isConnectViable() {
        // 当前socket是否处于可连接状态
        return Utils.isNetConnected(EasySocket.getInstance().getContext()) && connectionStatus.get() == SocketStatus.SOCKET_DISCONNECTED;
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
     * 发送bytes数据
     *
     * @param bytes
     * @return
     */
    private IConnectionManager sendBytes(byte[] bytes) {
        if (ioManager == null || connectionStatus.get() != SocketStatus.SOCKET_CONNECTED) {
            return this;
        }
        ioManager.sendBytes(bytes);
        return this;
    }

    @Override
    public void onCallBack(SuperCallBack callBack) {
        callbackResponseDispatcher.addSocketCallback(callBack);
    }


    @Override
    public synchronized IConnectionManager upBytes(byte[] bytes) {
        sendBytes(bytes);
        return this;
    }

    @Override
    public synchronized IConnectionManager upCallbackMessage(SuperCallbackSender sender) {
        callbackResponseDispatcher.checkCallbackSender(sender);
        // 发送
        sendBytes(sender.pack());
        return this;
    }


    @Override
    public HeartManager getHeartManager() {
        return heartManager;
    }
}
