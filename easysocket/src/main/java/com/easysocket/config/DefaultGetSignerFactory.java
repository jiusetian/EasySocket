package com.easysocket.config;

import com.easysocket.entity.OriginReadData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author：Alex
 * Date：2019/12/6
 * Note：默认的获取回调消息的singer的实现类
 */
public class DefaultGetSignerFactory extends GetSignerFactory {

    @Override
    public String getCallbackSigner(OriginReadData originReadData) {
        try {
            String data=originReadData.getBodyString();
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.getString("signer");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
