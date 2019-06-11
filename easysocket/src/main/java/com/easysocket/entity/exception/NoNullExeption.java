package com.easysocket.entity.exception;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：不能为null的异常
 */
public class NoNullExeption extends RuntimeException {
    public NoNullExeption(String e) {
        super(e);
    }
}
