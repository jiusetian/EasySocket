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
import com.easysocket.message.CallbackSender;
import com.easysocket.message.ClientHeartBeat;
import com.easysocket.message.TestMessage;
import com.easysocket.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 是否已连接
    private boolean isConnected;
    // 连接或断开按钮
    private Button controlConnect;
    // 是否已连接
    private boolean isConnected9998;
    // 连接或断开按钮
    private Button controlConnect9998;
    // 9998端口的地址
    private String ADDRESS_9998;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ADDRESS_9998 = getApplicationContext().getResources().getString(R.string.local_ip) + ":9998";

        controlConnect = findViewById(R.id.control_conn);
        controlConnect9998 = findViewById(R.id.control_conn1);

        View[] views = {controlConnect, findViewById(R.id.create_conn), findViewById(R.id.send_msg)
                , findViewById(R.id.start_heart), findViewById(R.id.progress_msg),
                findViewById(R.id.callback_msg), findViewById(R.id.destroy_conn)
                , controlConnect9998, findViewById(R.id.create_conn1), findViewById(R.id.send_msg1)
                , findViewById(R.id.start_heart1), findViewById(R.id.destroy_conn1)};
        // 点击事件
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }

    // 点击事件
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // 创建socket连接
            case R.id.create_conn:
                if (isConnected) {
                    Toast.makeText(MainActivity.this, "Socket已连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 初始化socket
                initEasySocket();
                // 监听socket行为
                EasySocket.getInstance().subscribeSocketAction(socketActionListener);
                break;

            // 发送一个测试消息
            case R.id.send_msg:
                sendTestMessage();
                break;

            // 发送有回调功能的消息
            case R.id.callback_msg:
                sendCallbackMsg();
                break;

            // 启动心跳检测
            case R.id.start_heart:
                startHeartbeat();
                break;

            // 有进度条的消息
            case R.id.progress_msg:
                sendProgressMsg();
                break;

            // 连接或断开连接
            case R.id.control_conn:
                if (isConnected) {
                    EasySocket.getInstance().disconnect(false);
                } else {
                    EasySocket.getInstance().connect();
                    //EasySocket.getInstance().subscribeSocketAction(socketActionListener);
                }
                break;

            // 销毁socket连接
            case R.id.destroy_conn:
                EasySocket.getInstance().destroyConnection();
                break;

            //=========================下面是连接9998端口的测试============================
            // 创建socket连接【9998端口】
            case R.id.create_conn1:
                if (isConnected9998) {
                    Toast.makeText(MainActivity.this, "Socket已连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 初始化socket
                initEasySocket9998();
                // 监听socket行为
                EasySocket.getInstance().subscribeSocketAction(socketActionListener, ADDRESS_9998);
                break;

            // 发送一个测试消息【9998端口】
            case R.id.send_msg1:
                sendTestMessage(ADDRESS_9998);
                break;

            // 启动心跳检测【9998端口】
            case R.id.start_heart1:
                startHeartbeat(ADDRESS_9998);
                break;

            // 连接或断开连接【9998端口】
            case R.id.control_conn1:
                if (isConnected9998) {
                    EasySocket.getInstance().disconnect(ADDRESS_9998, false);
                } else {
                    EasySocket.getInstance().connect(ADDRESS_9998);
                    //EasySocket.getInstance().subscribeSocketAction(socketActionListener);
                }
                break;

            // 销毁socket连接【9998端口】
            case R.id.destroy_conn1:
                EasySocket.getInstance().destroyConnection(ADDRESS_9998);
                break;
        }
    }

    // 有进度条的消息
    private void sendProgressMsg() {

        // 进度条接口
        IProgressDialog progressDialog = new IProgressDialog() {
            @Override
            public Dialog getDialog() {
                ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle("正在加载...");
                return dialog;
            }
        };
        CallbackSender sender = new CallbackSender();
        sender.setFrom("android");
        sender.setMsgId("delay_msg");
        EasySocket.getInstance()
                .upCallbackMessage(sender)
                .onCallBack(new ProgressDialogCallBack(progressDialog, true, true, sender.getCallbackId()) {
                    @Override
                    public void onResponse(OriginReadData data) {
                        LogUtil.d("进度条回调消息-->" + data.getBodyString());
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
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
                .onCallBack(new SimpleCallBack(sender.getCallbackId()) {
                    @Override
                    public void onResponse(OriginReadData data) {
                        LogUtil.d("Socket应答消息-->" + data.getBodyString());
                        Toast.makeText(MainActivity.this, "应答消息：" + data.getBodyString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
                    }
                });
    }

    // 启动心跳检测功能
    private void startHeartbeat(String address) {
        // 心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat.pack(), address, new HeartManager.HeartbeatListener() {
            // 用于判断当前收到的信息是否为服务器心跳，根据自己的实际情况实现
            @Override
            public boolean isServerHeartbeat(OriginReadData orginReadData) {
                try {
                    String s = orginReadData.getBodyString();
                    JSONObject jsonObject = new JSONObject(s);
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

    // 启动心跳检测功能
    private void startHeartbeat() {
        // 心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat.pack(), new HeartManager.HeartbeatListener() {
            // 用于判断当前收到的信息是否为服务器心跳，根据自己的实际情况实现
            @Override
            public boolean isServerHeartbeat(OriginReadData orginReadData) {
                try {
                    String s = orginReadData.getBodyString();
                    JSONObject jsonObject = new JSONObject(s);
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
    private void sendTestMessage() {
        TestMessage testMessage = new TestMessage();
        testMessage.setMsgId("test_msg");
        testMessage.setFrom("android");
        // 发送
        EasySocket.getInstance().upMessage(testMessage.pack());
    }

    /**
     * 发送一个的消息，
     */
    private void sendTestMessage(String address) {
        TestMessage testMessage = new TestMessage();
        testMessage.setMsgId("test_msg");
        testMessage.setFrom("android");
        // 发送
        EasySocket.getInstance().upMessage(testMessage.pack(), address);
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
            LogUtil.d("端口" + socketAddress.getPort() + "---> 连接成功");
            if (socketAddress.getPort() == 9998) {
                controlConnect9998.setText(socketAddress.getPort() + "端口" + "socket已连接，点击断开连接");
                isConnected9998 = true;
            } else {
                controlConnect.setText(socketAddress.getPort() + "端口" + "socket已连接，点击断开连接");
                isConnected = true;
            }
        }

        /**
         * socket连接失败
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
            controlConnect.setText(socketAddress.getPort() + "端口" + "socket连接被断开，点击进行连接");
            if (socketAddress.getPort() == 9998) {
                isConnected9998 = false;
            } else {
                isConnected = false;
            }

        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(socketAddress.getPort() + "端口" + "---> socket断开连接，是否需要重连：" + isNeedReconnect);
            controlConnect.setText(socketAddress.getPort() + "端口" + "socket连接被断开，点击进行连接");
            if (socketAddress.getPort() == 9998) {
                isConnected9998 = false;
            } else {
                isConnected = false;
            }

        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param readData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, String readData) {
            LogUtil.d(socketAddress.getPort() + "端口" + "SocketActionListener收到数据-->" + readData);
        }

        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d(socketAddress.getPort() + "端口" + "SocketActionListener收到数据-->" + originReadData.getBodyString());
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
                .setCallbackIDFactory(new CallbackIDFactoryImpl())
                // 定义消息协议，方便解决 socket黏包、分包的问题，如果客户端定义了消息协议，那么
                // 服务端也要对应对应的消息协议，如果这里没有定义消息协议，服务端也不需要定义
                .setReaderProtocol(new DefaultMessageProtocol())
                .build();

        // 初始化
        EasySocket.getInstance()
                .createConnection(options, this);// 创建一个socket连接
    }

    /**
     * 初始化EasySocket[9998端口]
     */
    private void initEasySocket9998() {
        // socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setCallbackIDFactory(new CallbackIDFactoryImpl())
                // 主机地址，请填写自己的IP地址，以getString的方式是为了隐藏作者自己的IP地址
                .setSocketAddress(new SocketAddress(getResources().getString(R.string.local_ip), 9998))
                // 定义消息协议，方便解决 socket黏包、分包的问题，如果客户端定义了消息协议，那么
                // 服务端也要对应对应的消息协议，如果这里没有定义消息协议，服务端也不需要定义
                .setReaderProtocol(new DefaultMessageProtocol())
                .build();

        // 初始化
        EasySocket.getInstance().createSpecifyConnection(options, this);// 创建一个socket连接
    }
}
