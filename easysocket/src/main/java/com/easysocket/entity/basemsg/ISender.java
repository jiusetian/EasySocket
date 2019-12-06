package com.easysocket.entity.basemsg;

import java.io.Serializable;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：发送数据的接口
 */
public interface ISender extends Serializable {

    /**
     * 打包要发送的数据
     * @return
     */
    byte[] parse();

}
