package com.easysocket;

import com.easysocket.config.CallbackIDFactory;
import com.easysocket.entity.OriginReadData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author：枪花
 * Date：2020/3/20
 * note：根据自己的实际情况实现
 */
public class CallbackIDFactoryImpl extends CallbackIDFactory {

    /**
     * @param
     * @return
     */
    @Override
    public String getCallbackID(OriginReadData data) {
        try {
            JSONObject body = new JSONObject(data.getBodyString());
            String callbackId = body.getString("callbackId");
            return callbackId;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}
