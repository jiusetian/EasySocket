package com.easysocket.interfaces.io;

import java.io.IOException;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public interface IWriter<T> {
    /**
     * 保存要写的数据
     */
    void offer(byte[] sender);

    /**
     * 写数据
     * @param sender
     */
    void write(byte[] sender) throws IOException;

    /**
     * 关闭stream
     */
    void closeWriter();

    /**
     * 开启写数据
     */
    void openWriter();

    /**
     * 设置参数
     * @param t
     */
    void setOption(T t);

}
