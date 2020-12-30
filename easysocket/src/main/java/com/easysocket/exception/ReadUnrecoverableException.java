package com.easysocket.exception;

/**
 * Author：Mapogo
 * Date：2020/12/29
 * Note：不可修复的读取错误
 */
public class ReadUnrecoverableException extends Exception {
    public ReadUnrecoverableException(String s) {
        super(s);
    }
}
