package com.easysocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.DefaultSender;
import com.easysocket.entity.IsReconnect;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化socket
        initEasySocket();

        //监听socket相关行为
        EasySocket.getInstance().subscribeSocketAction(socketActionListener);

        //发送一个心跳包
        findViewById(R.id.send_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHeartBeat();
            }
        });

        //激活自动发送心跳
        findViewById(R.id.auto_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
                clientHeartBeat.setMsgId("heart_beat");
                clientHeartBeat.setFrom("client");
                EasySocket.getInstance().getConnection().getHeartBeatManager().startHeartbeat(clientHeartBeat);
            }
        });

        findViewById(R.id.act_callback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CallBackActivity.class));
            }
        });

    }

    /**
     * socket行为监听
     */
    private ISocketActionListener socketActionListener = new SocketActionListener() {
        /**
         * socket连接成功
         * @param socketAddress
         */
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            super.onSocketConnSuccess(socketAddress);
            LogUtil.d("连接成功");
        }

        /**
         * socket连接失败
         * @param socketAddress
         * @param isReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, IsReconnect isReconnect) {
            super.onSocketConnFail(socketAddress, isReconnect);
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, IsReconnect isReconnect) {
            super.onSocketDisconnect(socketAddress, isReconnect);
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d("监听器接收的数据->" + originReadData.getBodyString());
            //演示接收到服务端心跳
            EasySocket.getInstance().getConnection().getHeartBeatManager().onReceiveHeartBeat();
        }
    };


    /**
     * 发送心跳包
     */
    private void sendHeartBeat() {
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        DefaultSender defaultSender = new DefaultSender(clientHeartBeat);
        //发送
        EasySocket.getInstance().upObject(defaultSender);
    }

    /**
     * 初始化EasySocket
     */
    private void initEasySocket() {

        //socket配置为默认值
        EasySocketOptions options = new EasySocketOptions.Builder()
                .build();

        //初始化EasySocket
        EasySocket.getInstance()
                .ip("192.168.4.52") //IP地址
                .port(9999) //端口
                .options(options) //连接的配置
                .buildConnection(); //创建一个socket连接
    }
}
