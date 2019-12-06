package com.easysocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.NeedReconnect;
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

        //监听socket行为
        EasySocket.getInstance().subscribeSocketAction(socketActionListener);

        //发送一个消息
        findViewById(R.id.send_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        //跳转到具有回调功能的act
        findViewById(R.id.act_callback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CallBackActivity.class));
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        //EasySocket.getInstance().destroyConnection();
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
         * @param needReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, NeedReconnect needReconnect) {
            super.onSocketConnFail(socketAddress, needReconnect);
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param needReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, NeedReconnect needReconnect) {
            super.onSocketDisconnect(socketAddress, needReconnect);
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d("socket接收的数据->" + originReadData.getBodyString());
        }
    };


    /**
     * 发送一个没有回调的消息，即没有回调标识singer的消息
     */
    private void sendMessage() {
        TestMsg testMsg =new TestMsg();
        testMsg.setMsgId("no_singer_msg");
        testMsg.setFrom("android");
        //发送
        EasySocket.getInstance().upObject(testMsg);
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
                .ip("192.168.3.9") //IP地址
                .port(9999) //端口
                .options(options) //连接的配置
                .buildConnection(); //创建一个socket连接
    }
}
