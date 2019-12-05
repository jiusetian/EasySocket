package com.socker_server;

import com.google.gson.Gson;
import com.socker_server.entity.AbsReceiveMsg;
import com.socker_server.entity.AbsSendMsg;
import com.socker_server.entity.MsgId;
import com.socker_server.entity.WrapperSender;
import com.socker_server.entity.MyResponse;
import com.socker_server.entity.ServerHeartBeat;
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
        AbsReceiveMsg receiveMsg=new Gson().fromJson(receiver,AbsReceiveMsg.class);
        String id = receiveMsg.getMsgId(); //消息ID
        String ack = receiveMsg.getSinger(); //作为本地反馈消息的唯一标识ID
        AbsSendMsg sendMsg = null;

        switch (id) {
            case MsgId.HEARTBEAT: //心跳包
                sendMsg = new ServerHeartBeat();
                ((ServerHeartBeat) sendMsg).setFrom("server");
                sendMsg.setSinger(ack);
                sendMsg.setMsgId(MsgId.HEARTBEAT);
                break;

            case MsgId.MY_REQUEST:
                sendMsg=new MyResponse();
                sendMsg.setSinger(ack);
                sendMsg.setMsgId(MsgId.MY_REQUEST);
                ((MyResponse)sendMsg).setFrom("server");
                try {
                    Thread.sleep(1000*10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

        }

        if (sendMsg == null) return;
        String backStr = new Gson().toJson(sendMsg);
        System.out.println("send message:"+backStr);
        WrapperSender packet=new WrapperSender(backStr);
        easyWriter.offer(packet.parse());
    }

}
