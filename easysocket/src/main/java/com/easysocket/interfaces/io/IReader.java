package com.easysocket.interfaces.io;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public interface IReader<T> {

    /**
     * 读数据
     */
    void read() throws Exception;

    /**
     * 打开数据的读取
     */
    void openReader();

    /**
     * 关闭数据的读取
     */
    void closeReader();

    /**
     * 设置参数
     * @param t
     */
    void setOption(T t);


}
