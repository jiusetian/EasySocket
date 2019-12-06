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
        EasySocket.getInstance().destroyConnection();
        initCallbackSocket();

        //发送有回调的消息
        findViewById(R.id.send_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSingerMsg();
            }
        });


        //有进度条的请求
        findViewById(R.id.send_progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingerSender sender = new SingerSender();
                sender.setFrom("android");
                sender.setMsgId("delay_msg");
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
     * 发送一个有回调的消息
     */
    private void sendSingerMsg() {

        SingerSender sender=new SingerSender();
        sender.setMsgId("singer_msg");
        sender.setFrom("android");
        EasySocket.getInstance().upObject(sender)
                .onCallBack(new SimpleCallBack<SingerResponse>(sender) {
                    @Override
                    public void onResponse(SingerResponse response) {
                        LogUtil.d("回调消息="+response.toString());
                    }
                });

    }

    /**
     * 初始化具有回调功能的socket
     */
    private void initCallbackSocket() {
        //心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");

        //socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setActiveHeart(true) //启动心跳检测功能
                .setEnableCallback(true) //启动消息的回调功能
                .setClientHeart(clientHeartBeat) //设置心跳对象
                .build();

        //初始化EasySocket
        EasySocket.getInstance()
                .ip("192.168.3.9") //IP地址
                .port(9999) //端口
                .options(options) //连接的配置
                .buildConnection(); //创建一个socket连接

    }
}
