package com.easysocket.entity;

import java.io.Serializable;

/**
 * Author：Alex
 * Date：2019/6/3
 * Note：是否需要重连
 */
public class IsReconnect implements Serializable {

    private boolean isNeedRecon;

    public IsReconnect(boolean isNeedRecon) {
        this.isNeedRecon=isNeedRecon;
    }

    public boolean booleanValue(){
        return isNeedRecon;
    }
}
