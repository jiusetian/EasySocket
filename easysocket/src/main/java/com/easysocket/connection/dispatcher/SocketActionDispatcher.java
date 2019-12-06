package com.easysocket.connection.dispatcher;

import com.easysocket.entity.NeedReconnect;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.OriginReadData;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.IConnectionManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.easysocket.connection.action.SocketAction.ACTION_CONN_FAIL;
import static com.easysocket.connection.action.SocketAction.ACTION_CONN_SUCCESS;
import static com.easysocket.connection.action.SocketAction.ACTION_DISCONNECTION;
import static com.easysocket.connection.action.IOAction.ACTION_READ_COMPLETE;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：连接行为的分发器
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
     * 行为回调集合
     */
    private List<ISocketActionListener> actionListeners = new ArrayList<>();
    /**
     * 处理回调信息的线程
     */
    private Thread actionThread;

    /**
     * 事件消费队列
     */
    private static final LinkedBlockingQueue<ActionBean> actions = new LinkedBlockingQueue();

    public SocketActionDispatcher(IConnectionManager connectionManager, SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        this.connectionManager = connectionManager;
        //开启线程处理回调信息
        actionThread=new DispatchThread();
        actionThread.start();
    }

    public void setSocketAddress(SocketAddress info){
        socketAddress =info;
    }

    @Override
    public void dispatchAction(String action) {
        dispatchAction(action, null);
    }

    @Override
    public void dispatchAction(String action, Serializable serializable) {
        //首先装入行为队列中
        ActionBean actionBean = new ActionBean(action, serializable, this);
        actions.offer(actionBean);
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
    private static class DispatchThread extends Thread {
        boolean isStop;

        public DispatchThread() {
            super("dispatch thread");
            isStop = false;
        }

        @Override
        public void run() {
            //循环处理socket的行为信息
            while (!isStop) {
                try {
                    ActionBean actionBean = actions.take();
                    if (actionBean != null && actionBean.mDispatcher != null) {
                        SocketActionDispatcher actionDispatcher = actionBean.mDispatcher;
                        synchronized (actionDispatcher.actionListeners) {
                            List<ISocketActionListener> copyData = new ArrayList<>(actionDispatcher.actionListeners);
                            Iterator<ISocketActionListener> it = copyData.iterator();
                            //逐一通知
                            while (it.hasNext()) {
                                ISocketActionListener listener = it.next();
                                actionDispatcher.dispatchActionToListener(actionBean.mAction, actionBean.arg, listener);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 行为封装
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
    private void dispatchActionToListener(String action, Serializable content, ISocketActionListener actionListener) {
        switch (action) {
            case ACTION_CONN_SUCCESS: //连接成功
                actionListener.onSocketConnSuccess(socketAddress);
                break;

            case ACTION_CONN_FAIL: //连接失败
                actionListener.onSocketConnFail(socketAddress, (NeedReconnect) content);
                break;

            case ACTION_DISCONNECTION: //连接断开
                actionListener.onSocketDisconnect(socketAddress, (NeedReconnect) content);
                break;

            case ACTION_READ_COMPLETE: //读取完成
                actionListener.onSocketResponse(socketAddress, (OriginReadData) content);
                break;
        }
    }
}
