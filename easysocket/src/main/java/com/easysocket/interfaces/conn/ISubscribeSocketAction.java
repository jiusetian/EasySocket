package com.easysocket.interfaces.conn;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：订阅监听socket
 */
public interface ISubscribeSocketAction {
    /**
     * 注册监听socket的行为
     * @param iSocketActionListener
     */
    void subscribeSocketAction(ISocketActionListener iSocketActionListener);

    /**
     * 注销监听socket的行为
     * @param iSocketActionListener
     */
    void unSubscribeSocketAction(ISocketActionListener iSocketActionListener);
}
