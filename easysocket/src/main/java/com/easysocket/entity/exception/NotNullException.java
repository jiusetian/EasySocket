package com.easysocket.entity.exception;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：不能为null的异常
 */
public class NotNullException extends RuntimeException {
    public NotNullException(String e) {
        super(e);
    }
}
