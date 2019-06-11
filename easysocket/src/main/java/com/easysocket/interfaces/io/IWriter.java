package com.easysocket.interfaces.io;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public interface IWriter<T> {
    /**
     * 保存需要写入的数据
     */
    void offer(byte[] sender);

    /**
     * 写入数据
     * @param sender
     */
    void write(byte[] sender);

    /**
     * 关闭stream
     */
    void closeWriter();

    /**
     * 打开读取数据
     */
    void openWriter();

    /**
     * 设置参数
     * @param t
     */
    void setOption(T t);

}
