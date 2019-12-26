package com.easysocket.entity.exception;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：非空异常
 */
public class NoNullException extends RuntimeException {
    public NoNullException(String e) {
        super(e);
    }
}
