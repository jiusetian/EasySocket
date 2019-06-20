package com.easysocket;

import com.easysocket.config.AckFactory;
import com.easysocket.entity.OriginReadData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：实现获取ack的工厂
 */
public class AckFactoryImpl extends AckFactory {
    @Override
    public String createCallbackAck(OriginReadData originReadData) {
        try {
            String data=originReadData.getBodyString();
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.getString("ack");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
