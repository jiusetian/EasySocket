package com.socker_server;

import com.socker_server.entity.DefaultMessageProtocol;
import com.socker_server.iowork.ServerIOManager;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {

    private static final int PORT1 = 9999;
    private static final int PORT2 = 9998;
    private List<Socket> mList = new ArrayList<Socket>();
//    private ServerSocket server9999 = null;
//    private ServerSocket server9998 = null;
    private ExecutorService mExecutorService = null;
    private ExecutorService launchService;

    public static void main(String[] args) {
        new MainClass();
        System.out.println("java running");
    }

    public MainClass() {
        mExecutorService = Executors.newCachedThreadPool();
        launchService = Executors.newFixedThreadPool(2);
        // 开启了两个端口的socket服务
        launchService.execute(new Runnable() {
            @Override
            public void run() {
                initServerSocket(PORT1);
            }
        });
        launchService.execute(new Runnable() {
            @Override
            public void run() {
                initServerSocket(PORT2);
            }
        });
    }

    private void initServerSocket(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            initConfig(); // 配置信息
            System.out.println("端口" + port + "server is running");
            Socket client;
            while (true) {
                client = server.accept();
                mList.add(client);
                mExecutorService.execute(new Service(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initConfig() {
        // 默认的消息协议
        ServerConfig.getInstance().setMessageProtocol(new DefaultMessageProtocol());
    }

    class Service implements Runnable {
        private Socket socket;
        ServerIOManager serverIoManager;

        public Service(Socket socket) {
            this.socket = socket;
            System.out.println("connect server sucessful: " + socket.getInetAddress().getHostAddress());
        }

        @Override
        public void run() {
            System.out.println("运行io");
            serverIoManager = new ServerIOManager(socket);
            serverIoManager.startIO();
        }
    }

}
