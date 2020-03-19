package com.socker_server.entity.message;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：带有singer的服务端消息
 */
public class SignerServerMsg extends ServerMsg{

    /**
     * 回调标识
     */
    private String signer;

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }
}
