package com.easysocket;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.easysocket.callback.ProgressDialogCallBack;
import com.easysocket.callback.SimpleCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.IsNeedReconnect;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

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
        findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //发送有回调功能的消息
        findViewById(R.id.callback_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSingerMsg();
            }
        });

        //启动心跳检测
        findViewById(R.id.start_heart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHeartbeat();
            }
        });


        //有进度条的消息
        findViewById(R.id.progress_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallbackSender sender = new CallbackSender();
                sender.setFrom("android");
                sender.setMsgId("delay_msg");
                EasySocket.getInstance()
                        .upCallbackMessage(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender) {
                            @Override
                            public void onResponse(String s) {
                                LogUtil.d("进度条回调消息=" + s);
                            }

                            @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                e.printStackTrace();
                            }
                        });
            }
        });
    }

    private IProgressDialog progressDialog = new IProgressDialog() {
        @Override
        public Dialog getDialog() {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setTitle("正在加载...");
            return dialog;
        }
    };


    /**
     * 发送一个有回调的消息
     */
    private void sendSingerMsg() {

        CallbackSender sender = new CallbackSender();
        sender.setMsgId("singer_msg");
        sender.setFrom("android");
        EasySocket.getInstance().upCallbackMessage(sender)
                .onCallBack(new SimpleCallBack<CallbackResponse>(sender) {
                    @Override
                    public void onResponse(CallbackResponse response) {
                        LogUtil.d("回调消息=" + response.toString());
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
                    }
                });
    }

    //启动心跳检测功能
    private void startHeartbeat() {
        //心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat, new HeartManager.HeartbeatListener() {
            @Override
            public boolean isServerHeartbeat(OriginReadData originReadData) {
                String msg = originReadData.getBodyString();
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    if ("heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("收到服务器心跳");
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    /**
     * 发送一个的消息，
     */
    private void sendMessage() {
        TestMsg testMsg = new TestMsg();
        testMsg.setMsgId("no_singer_msg");
        testMsg.setFrom("android");
        //发送
        EasySocket.getInstance().upObject(testMsg);
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
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, IsNeedReconnect isNeedReconnect) {
            super.onSocketConnFail(socketAddress, isNeedReconnect);
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, IsNeedReconnect isNeedReconnect) {
            super.onSocketDisconnect(socketAddress, isNeedReconnect);
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d("socket监听器收到数据=" + originReadData.getBodyString());
        }
    };


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
                .options(options); //连接的配置

        //创建一个socket连接
        EasySocket.getInstance().buildConnection();
    }
}
