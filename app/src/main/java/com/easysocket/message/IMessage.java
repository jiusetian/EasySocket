package com.easysocket.message;

/**
 * Author：Mapogo
 * Date：2021/1/13
 * Note：所有消息的接口
 */
public interface IMessage {

    /**
     * 根据自己的协议打包消息
     *
     * @return 返回打包好的byte[]
     */
    byte[] pack();
}
