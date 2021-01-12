package com.easysocket.entity;

import com.easysocket.EasySocket;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：读到的数据
 */
public class OriginReadData implements Serializable {

    /**
     * 包头数据
     */
    private byte[] headerData;
    /**
     * 包体数据
     */
    private byte[] bodyData;

    public byte[] getHeaderData() {
        return headerData;
    }

    public void setHeaderData(byte[] headerData) {
        this.headerData = headerData;
    }

    public byte[] getBodyBytes() {
        return bodyData;
    }

    public void setBodyData(byte[] bodyData) {
        this.bodyData = bodyData;
    }

    /**
     * 获取数据body的string
     * @return
     */
    public String getBodyString(){
        return new String(getBodyBytes(), Charset.forName(EasySocket.getInstance().getOptions().getCharsetName()));
    }
}
