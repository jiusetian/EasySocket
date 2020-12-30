package com.easysocket.exception;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：非空异常
 */
public class NotNullException extends RuntimeException {
    public NotNullException(String e) {
        super(e);
    }
}
