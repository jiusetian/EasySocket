package com.easysocket.exception;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：可恢复socket读数据异常
 */
public class ReadRecoverableExeption extends Exception {

    public ReadRecoverableExeption(String s){
        super(s);
    }
}
