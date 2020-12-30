package com.easysocket;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.easysocket.callback.ProgressDialogCallBack;
import com.easysocket.callback.SimpleCallBack;
import com.easysocket.config.DefaultMessageProtocol;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // 是否已连接
    private boolean isConnected;
    // 连接或断开按钮
    private Button controlConnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlConnect = findViewById(R.id.control_conn);

        // 创建socket连接
        findViewById(R.id.create_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    Toast.makeText(MainActivity.this, "Socket已连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 初始化socket
                initEasySocket();
                // 监听socket行为
                EasySocket.getInstance().subscribeSocketAction(socketActionListener);
            }
        });

        // 发送一个object
        findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // 发送一个string
        findViewById(R.id.send_string).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasySocket.getInstance().upString("how r u doing");
            }
        });

        // 发送有回调功能的消息
        findViewById(R.id.callback_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCallbackMsg();
            }
        });

        // 启动心跳检测
        findViewById(R.id.start_heart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHeartbeat();
            }
        });

        // 有进度条的消息
        findViewById(R.id.progress_msg).setOnClickListener(new View.OnClickListener() {
            // 进度条接口
            private IProgressDialog progressDialog = new IProgressDialog() {
                @Override
                public Dialog getDialog() {
                    ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setTitle("正在加载...");
                    return dialog;
                }
            };

            @Override
            public void onClick(View v) {
                CallbackSender sender = new CallbackSender();
                sender.setFrom("android");
                sender.setMsgId("delay_msg");
                EasySocket.getInstance()
                        .upCallbackMessage(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender.getCallbackId()) {
                            @Override
                            public void onResponse(String s) {
                                LogUtil.d("进度条回调消息-->" + s);
                            }

                            @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                e.printStackTrace();
                            }
                        });
            }
        });

        // 连接或断开连接
        controlConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    EasySocket.getInstance().disconnect(false);
                } else {
                    EasySocket.getInstance().connect();
                }
            }
        });

        // 销毁socket连接
        findViewById(R.id.destroy_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasySocket.getInstance().destroyConnection();
            }
        });
    }


    /**
     * 发送一个有回调的消息
     */
    private void sendCallbackMsg() {

        CallbackSender sender = new CallbackSender();
        sender.setMsgId("callback_msg");
        sender.setFrom("我来自android");
        EasySocket.getInstance().upCallbackMessage(sender)
                .onCallBack(new SimpleCallBack<CallbackResponse>(sender.getCallbackId()) {
                    @Override
                    public void onResponse(CallbackResponse response) {
                        LogUtil.d("Socket应答消息-->" + response.toString());
                        Toast.makeText(MainActivity.this, "应答消息：" + response.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
                    }
                });
    }

    // 启动心跳检测功能
    private void startHeartbeat() {
        // 心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        // 心跳包类型可以是object、String、byte[]，HeartbeatListener用于判断接收的消息是不是服务端心跳
        EasySocket.getInstance().startHeartBeat(clientHeartBeat, new HeartManager.HeartbeatListener() {
            @Override
            public boolean isServerHeartbeat(OriginReadData originReadData) {
                String msg = originReadData.getBodyString();
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    if ("heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("---> 收到服务端心跳");
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
        TestMessage testMessage = new TestMessage();
        testMessage.setMsgId("test_msg");
        testMessage.setFrom("android");
        // 发送
        EasySocket.getInstance().upObject(testMessage);
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
            LogUtil.d("---> 连接成功");
            controlConnect.setText("socket已连接，点击断开连接");
            isConnected = true;
        }

        /**
         * socket连接失败
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
            super.onSocketConnFail(socketAddress, isNeedReconnect);
            controlConnect.setText("socket连接被断开，点击进行连接");
            isConnected = false;
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
            super.onSocketDisconnect(socketAddress, isNeedReconnect);
            LogUtil.d("---> socket断开连接，是否需要重连：" + isNeedReconnect);
            controlConnect.setText("socket连接被断开，点击进行连接");
            isConnected = false;
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d("SocketActionListener收到数据-->" + originReadData.getBodyString());
        }
    };


    /**
     * 初始化EasySocket
     */
    private void initEasySocket() {
        // socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                // 主机地址，请填写自己的IP地址，以getString的方式是为了隐藏作者自己的IP地址
                .setSocketAddress(new SocketAddress(getResources().getString(R.string.local_ip), 9999))
                .setCallbackKeyFactory(new CallbackKeyFactoryImpl())
                // 定义消息协议，方便解决 socket黏包、分包的问题
                .setReaderProtocol(new DefaultMessageProtocol())
                .build();

        // 初始化
        EasySocket.getInstance()
                .options(options) // 项目配置
                .createConnection();// 创建一个socket连接
    }
}
