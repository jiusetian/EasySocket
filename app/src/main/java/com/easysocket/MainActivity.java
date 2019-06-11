package com.easysocket;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.easysocket.callback.ProgressDialogCallBack;
import com.easysocket.callback.SimpleCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.DefaultSender;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.utils.ELog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEasySocket();

        //发送一个心跳包
        findViewById(R.id.send_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHeartBeat();
            }
        });

        //有进度条的请求
        findViewById(R.id.send_progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySender sender=new MySender();
                sender.setFrom("android");
                sender.setMsgId("my_request");
                DefaultSender defaultSender =new DefaultSender(sender);
                EasySocket.getInstance()
                        .upObject(defaultSender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog,true,true, defaultSender) {
                            @Override
                            public void onResponse(String s) {
                                ELog.d("请求返回的消息="+s);
                            }
                        });
            }
        });
    }

    private IProgressDialog progressDialog=new IProgressDialog() {
        @Override
        public Dialog getDialog() {
            Dialog dialog=new Dialog(MainActivity.this);
            dialog.setTitle("正在加载...");
            return dialog;
        }
    };

    /**
     * 发送心跳包
     */
    private void sendHeartBeat(){
        ClientHeartBeat clientHeartBeat=new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        DefaultSender defaultSender =new DefaultSender(clientHeartBeat);
        EasySocket.getInstance().upObject(defaultSender)
                .onCallBack(new SimpleCallBack<ServerHeartBeat>(defaultSender) {
                    @Override
                    public void onResponse(ServerHeartBeat serverHeartBeat) {
                        ELog.d("心跳包请求反馈："+serverHeartBeat.toString());
                    }
                });
    }

    /**
     * 初始化EasySocket
     */
    private void initEasySocket(){
        ClientHeartBeat clientHeartBeat=new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");

        //socket配置
        EasySocketOptions options=new EasySocketOptions.Builder()
                .setSignerFactory(new SignerFactoryImpl()) //设置获取请求标识signer的factory
                .setActiveHeart(true) //自动启动心跳管理器
                .setClientHeart(clientHeartBeat) //设置全局心跳对象
                .build();

        //初始化EasySocket
        EasySocket.getInstance()
                .mainIP("192.168.4.52") //自己的本地IP地址
                .mainPort(9999) //端口
                .mainOptions(options) //主连接的配置
                .buildMainConnection(); //创建一个主socket连接
    }
}
