package com.easysocket.entity;

import java.io.Serializable;

/**
 * Author：Alex
 * Date：2019/6/3
 * Note：是否需要重连
 */
public class IsNeedReconnect implements Serializable {

    private boolean isNeedRecon;

    public IsNeedReconnect(boolean isNeedRecon) {
        this.isNeedRecon=isNeedRecon;
    }

    public boolean booleanValue(){
        return isNeedRecon;
    }
}
