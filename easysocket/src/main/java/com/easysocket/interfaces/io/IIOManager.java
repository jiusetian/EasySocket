package com.easysocket.interfaces.io;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public interface IIOManager {

    /**
     * 发送字节流
     *
     * @param bytes
     */
    void sendBytes(byte[] bytes);

    /**
     * 关闭io管理器
     */
    void closeIO();

    /**
     * 开启io操作
     */
    void startIO();

}
