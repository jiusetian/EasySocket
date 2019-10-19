package com.easysocket;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.easysocket.callback.ProgressDialogCallBack;
import com.easysocket.callback.SimpleCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.utils.LogUtil;

/**
 * EasySocket回调功能的演示
 */
public class CallBackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_back);

        initCallbackSocket();

        //发送一个心跳包
        findViewById(R.id.send_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHeartBeat();
            }
        });

        //激活心跳
        findViewById(R.id.activate_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
                clientHeartBeat.setMsgId("heart_beat");
                clientHeartBeat.setFrom("client");
                EasySocket.getInstance().startHeartBeat(clientHeartBeat);
            }
        });

        //有进度条的请求
        findViewById(R.id.send_progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCallbackSender sender = new MyCallbackSender();
                sender.setFrom("android");
                sender.setMsgId("my_request");
                EasySocket.getInstance()
                        .upObject(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender) {
                            @Override
                            public void onResponse(String s) {
                                LogUtil.d("请求返回的消息=" + s);
                            }
                        });
            }
        });
    }

    private IProgressDialog progressDialog = new IProgressDialog() {
        @Override
        public Dialog getDialog() {
            Dialog dialog = new Dialog(CallBackActivity.this);
            dialog.setTitle("正在加载...");
            return dialog;
        }
    };

    /**
     * 发送心跳包
     */
    private void sendHeartBeat() {
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().upObject(clientHeartBeat)
                .onCallBack(new SimpleCallBack<ServerHeartBeat>(clientHeartBeat) {
                    @Override
                    public void onResponse(ServerHeartBeat serverHeartBeat) {
                        LogUtil.d("心跳包请求反馈：" + serverHeartBeat.toString());
                    }
                });
    }

    /**
     * 初始化具有回调功能的socket
     */
    private void initCallbackSocket() {
        //心跳包实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");

        //socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setAckFactory(new AckFactoryImpl()) //设置获取请求标识signer的factory
                .setActiveHeart(true) //启动心跳管理器
                .setActiveResponseDispatch(true) //启动消息的回调管理
                .setClientHeart(clientHeartBeat) //设置全局心跳对象
                .build();

        //初始化EasySocket
        EasySocket.getInstance()
                .ip("192.168.3.9") //IP地址
                .port(9999) //端口
                .options(options) //连接的配置
                .buildConnection(); //创建一个socket连接

    }
}
