package com.socker_server;

import com.socker_server.iowork.IOManager;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {

    private static final int PORT = 9999;
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService mExecutorService = null;

    public static void main(String[] args) {
        new MainClass();
        System.out.println("java running");
    }

    public MainClass() {
        try {
            server = new ServerSocket(PORT);
            mExecutorService = Executors.newCachedThreadPool();
            initConfig(); // 初始化配置信息
            System.out.println("server is running");
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
        //ServerConfig.getInstance().setMessageProtocol(new DefaultMessageProtocol());
    }

    class Service implements Runnable {
        private Socket socket;
        IOManager ioManager;

        public Service(Socket socket) {
            this.socket = socket;
            System.out.println("connect server sucessful: " + socket.getInetAddress().getHostAddress());
        }

        @Override
        public void run() {
            ioManager = new IOManager(socket);
            ioManager.startIO();
        }
    }

}
