package com.easysocket;

import com.easysocket.config.CallbackSingerFactory;
import com.easysocket.entity.OriginReadData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：实现获取回调标识的工厂
 */
public class CallbackSingerFactoryImpl extends CallbackSingerFactory {
    @Override
    public String getCallbackSinger(OriginReadData originReadData) {
        try {
            String data=originReadData.getBodyString();
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.getString("singer");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
