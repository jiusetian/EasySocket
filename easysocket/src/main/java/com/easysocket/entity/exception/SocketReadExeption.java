package com.easysocket.entity.exception;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：socket读数据异常
 */
public class SocketReadExeption extends RuntimeException {

    public SocketReadExeption(String s){
        super(s);
    }
}
