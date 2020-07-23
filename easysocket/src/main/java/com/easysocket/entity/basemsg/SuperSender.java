package com.easysocket.entity.basemsg;

import com.easysocket.EasySocket;

/**
 * Author：Alex
 * Date：2019/10/19
 * Note：基础消息
 */
public class SuperSender implements ISender {

    @Override
    public final byte[] parse() {
        return EasySocket.getInstance().getOptions().getMessageProtocol().parse(this);
    }
}
