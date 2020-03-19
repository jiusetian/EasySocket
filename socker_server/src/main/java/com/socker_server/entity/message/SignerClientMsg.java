package com.socker_server.entity.message;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：带有singer的客户端消息
 */
public class SignerClientMsg extends ClientMsg {

    /**
     * 回调标识
     */
    private String signer;

    public String getSigner() {
        return signer;
    }

    public void setSigner(String backSign) {
        this.signer = backSign;
    }
}
