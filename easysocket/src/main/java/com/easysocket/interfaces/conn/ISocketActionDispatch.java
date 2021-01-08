package com.easysocket.interfaces.conn;

import java.io.Serializable;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：socket行为分发接口
 */
public interface ISocketActionDispatch {
    /**
     * 停止分发线程
     */
    void stopDispatchThread();

    void startDispatchThread();

    void dispatchAction(String action);

    /**
     * socket行为的分发
     * @param action
     * @param serializable
     */
    void dispatchAction(String action, Serializable serializable);

    /**
     * 订阅socket行为
     * @param iSocketActionListener
     */
    void subscribe(ISocketActionListener iSocketActionListener);

    /**
     * 解除socket行为的订阅
     * @param iSocketActionListener
     */
    void unsubscribe(ISocketActionListener iSocketActionListener);
}
