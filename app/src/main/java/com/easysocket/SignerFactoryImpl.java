package com.easysocket;

import com.easysocket.config.SignerFactory;
import com.easysocket.entity.OriginReadData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：实现signer工厂
 */
public class SignerFactoryImpl extends SignerFactory {
    @Override
    public String createCallbackSgin(OriginReadData originReadData) {
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
