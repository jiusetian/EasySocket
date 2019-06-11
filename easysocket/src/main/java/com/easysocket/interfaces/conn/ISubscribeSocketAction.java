package com.easysocket.interfaces.conn;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：订阅socket行为的监听
 */
public interface ISubscribeSocketAction {
    /**
     * 注册连接的行为监听
     * @param iSocketActionListener
     */
    void subscribeSocketAction(ISocketActionListener iSocketActionListener);

    /**
     * 解除连接行为监听
     * @param iSocketActionListener
     */
    void unSubscribeSocketAction(ISocketActionListener iSocketActionListener);
}
