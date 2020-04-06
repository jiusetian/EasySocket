package com.socker_server;

import com.google.gson.Gson;
import com.socker_server.entity.MessageID;
import com.socker_server.entity.message.CallbackResponse;
import com.socker_server.entity.message.DelayResponse;
import com.socker_server.entity.message.ServerHeartBeat;
import com.socker_server.entity.message.TestResponse;
import com.socker_server.entity.message.base.SuperClient;
import com.socker_server.entity.message.base.SuperResponse;
import com.socker_server.iowork.IWriter;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：处理io信息
 */
public class HandlerIO {

    private IWriter easyWriter;

    public HandlerIO(IWriter easyWriter) {
        this.easyWriter = easyWriter;
    }

    /**
     * 处理接收的信息
     * @param receiver
     */
    public void handReceiveMsg(String receiver) {
        System.out.println("receive message:"+receiver);
        SuperClient clientMsg =new Gson().fromJson(receiver, SuperClient.class);
        String id = clientMsg.getMsgId(); //消息ID
        String callbackId =clientMsg.getCallbackId(); //回调ID
        SuperResponse superResponse = null;

        switch (id) {
            case MessageID.CALLBACK_MSG: //回调消息
                superResponse =new CallbackResponse();
                ( superResponse).setCallbackId(callbackId);
                System.out.println("消息id="+callbackId);
                superResponse.setMsgId(MessageID.CALLBACK_MSG);
                ((CallbackResponse) superResponse).setFrom("server");
                break;

            case MessageID.TEST_MSG: //测试消息
                superResponse =new TestResponse();
                superResponse.setMsgId(MessageID.TEST_MSG);
                ((TestResponse) superResponse).setFrom("server");
                break;
            case MessageID.HEARTBEAT: //心跳包
                superResponse = new ServerHeartBeat();
                ((ServerHeartBeat) superResponse).setFrom("server");
                superResponse.setMsgId(MessageID.HEARTBEAT);
                break;

            case MessageID.DELAY_MSG: //延时消息
                superResponse =new DelayResponse();
                ( superResponse).setCallbackId(callbackId);
                superResponse.setMsgId(MessageID.DELAY_MSG);
                ((DelayResponse) superResponse).setFrom("server");
                try {
                    Thread.sleep(1000*5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }

        if (superResponse == null) return;
        System.out.println("send message:"+ convertObjectToJson(superResponse));
        easyWriter.offer(superResponse.parse());
    }



    private  String convertObjectToJson(Object object){
        Gson gson=new Gson();
        String json=gson.toJson(object);
        return json;
    }

}
