package com.socker_server;

import com.google.gson.Gson;
import com.socker_server.entity.MsgId;
import com.socker_server.entity.ServerHeartBeat;
import com.socker_server.entity.SingerResponse;
import com.socker_server.entity.WrapperSender;
import com.socker_server.entity.message.ServerMsg;
import com.socker_server.entity.message.ServerTestMsg;
import com.socker_server.entity.message.SingerClientMsg;
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
        SingerClientMsg clientMsg =new Gson().fromJson(receiver, SingerClientMsg.class);
        String id = clientMsg.getMsgId(); //消息ID
        String singer=clientMsg.getSinger(); //作为当前消息的返回的唯一标识ID
        ServerMsg serverMsg = null;

        switch (id) {
            case MsgId.SINGER_MSG: //带有singer的回调消息
                serverMsg =new SingerResponse();
                ((SingerResponse) serverMsg).setSinger(singer);
                serverMsg.setMsgId(MsgId.SINGER_MSG);
                ((SingerResponse) serverMsg).setFrom("server");
                break;

            case MsgId.NO_SINGER_MSG: //测试消息，不带singer
                serverMsg=new ServerTestMsg();
                serverMsg.setMsgId(MsgId.NO_SINGER_MSG);
                ((ServerTestMsg) serverMsg).setFrom("server");
                break;
            case MsgId.HEARTBEAT: //心跳包，带有singer
                serverMsg = new ServerHeartBeat();
                ((ServerHeartBeat) serverMsg).setFrom("server");
                ((ServerHeartBeat) serverMsg).setSinger(singer);
                serverMsg.setMsgId(MsgId.HEARTBEAT);
                break;

            case MsgId.DELAY_MSG: //延时消息
                serverMsg =new SingerResponse();
                ((SingerResponse) serverMsg).setSinger(singer);
                serverMsg.setMsgId(MsgId.DELAY_MSG);
                ((SingerResponse) serverMsg).setFrom("server");
                try {
                    Thread.sleep(1000*10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

        }

        if (serverMsg == null) return;
        String backStr = new Gson().toJson(serverMsg);
        System.out.println("send message:"+backStr);
        WrapperSender packet=new WrapperSender(backStr);
        easyWriter.offer(packet.parse());
    }

}
