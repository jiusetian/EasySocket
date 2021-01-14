package com.easysocket.connection.dispatcher;

import com.easysocket.EasySocket;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.utils.Utils;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.easysocket.connection.action.IOAction.ACTION_READ_COMPLETE;
import static com.easysocket.connection.action.SocketAction.ACTION_CONN_FAIL;
import static com.easysocket.connection.action.SocketAction.ACTION_CONN_SUCCESS;
import static com.easysocket.connection.action.SocketAction.ACTION_DISCONNECTION;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：socket行为分发器
 */
public class SocketActionDispatcher implements ISocketActionDispatch {
    /**
     * 连接地址
     */
    private SocketAddress socketAddress;
    /**
     * 连接器
     */
    private IConnectionManager connectionManager;
    /**
     * 回调监听集合
     */
    private List<ISocketActionListener> actionListeners = new ArrayList<>();
    /**
     * 处理socket行为的线程
     */
    private Thread actionThread;
    /**
     * 是否停止分发
     */
    private boolean isStop;

    /**
     * 事件消费队列
     */
    private final LinkedBlockingQueue<ActionBean> socketActions = new LinkedBlockingQueue();
    /**
     * 切换到UI线程
     */
    private MainThreadExecutor mainThreadExecutor = new MainThreadExecutor();


    public SocketActionDispatcher(IConnectionManager connectionManager, SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        this.connectionManager = connectionManager;
    }

    public void setSocketAddress(SocketAddress info) {
        socketAddress = info;
    }


    @Override
    public void dispatchAction(String action) {
        dispatchAction(action, null);
    }

    @Override
    public void dispatchAction(String action, Serializable serializable) {
        // 将接收到的socket行为封装入列
        ActionBean actionBean = new ActionBean(action, serializable, this);
        socketActions.offer(actionBean);
    }

    @Override
    public void subscribe(ISocketActionListener iSocketActionListener) {
        if (iSocketActionListener != null && !actionListeners.contains(iSocketActionListener)) {
            actionListeners.add(iSocketActionListener);
        }
    }

    @Override
    public void unsubscribe(ISocketActionListener iSocketActionListener) {
        actionListeners.remove(iSocketActionListener);
    }

    /**
     * 分发线程
     */
    private class DispatchThread extends Thread {

        public DispatchThread() {
            super("dispatch thread");
        }

        @Override
        public void run() {
            // 循环处理socket的行为信息
            while (!isStop) {
                try {
                    ActionBean actionBean = socketActions.take();
                    if (actionBean != null && actionBean.mDispatcher != null) {
                        SocketActionDispatcher actionDispatcher = actionBean.mDispatcher;
                        List<ISocketActionListener> copyListeners = new ArrayList<>(actionDispatcher.actionListeners);
                        Iterator<ISocketActionListener> listeners = copyListeners.iterator();
                        // 通知所有监听者
                        while (listeners.hasNext()) {
                            ISocketActionListener listener = listeners.next();
                            actionDispatcher.dispatchActionToListener(actionBean.mAction, actionBean.arg, listener);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * socket行为的封装
     */
    protected static class ActionBean {

        public ActionBean(String action, Serializable arg, SocketActionDispatcher dispatcher) {
            mAction = action;
            this.arg = arg;
            mDispatcher = dispatcher;
        }

        String mAction = "";
        Serializable arg;
        SocketActionDispatcher mDispatcher;
    }

    /**
     * 分发行为给监听者
     *
     * @param action
     * @param content
     * @param actionListener
     */
    private void dispatchActionToListener(String action, final Serializable content, final ISocketActionListener actionListener) {
        switch (action) {

            case ACTION_CONN_SUCCESS: // 连接成功
                mainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        actionListener.onSocketConnSuccess(socketAddress);
                    }
                });

                break;

            case ACTION_CONN_FAIL: // 连接失败
                mainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        actionListener.onSocketConnFail(socketAddress, ((Boolean) content).booleanValue());
                    }
                });

                break;

            case ACTION_DISCONNECTION: // 连接断开
                mainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        actionListener.onSocketDisconnect(socketAddress, ((Boolean) content).booleanValue());
                        // 不需要重连，则释放资源
                        if (!(Boolean) content) {
                            stopDispatchThread();
                        }
                    }
                });
                break;

            case ACTION_READ_COMPLETE: // 读取数据完成
                mainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // response有三种形式
                        actionListener.onSocketResponse(socketAddress, (OriginReadData) content);
                        byte[] data = Utils.concatBytes(((OriginReadData) content).getHeaderData(), ((OriginReadData) content).getBodyBytes());
                        actionListener.onSocketResponse(socketAddress, new String(data, Charset.forName(EasySocket.getInstance().getDefOptions().getCharsetName())));
                        actionListener.onSocketResponse(socketAddress, data);
                    }
                });
                break;
        }
    }

    // 开始分发线程
    @Override
    public void startDispatchThread() {
        isStop = false;
        if (actionThread == null) {
            actionThread = new DispatchThread();
            actionThread.start();
        }
    }

    @Override
    public void stopDispatchThread() {
        if (actionThread != null && actionThread.isAlive() && !actionThread.isInterrupted()) {
            socketActions.clear();
            //actionListeners.clear();
            isStop = true;
            actionThread.interrupt();
            actionThread = null;
        }
    }

}
