package com.easysocket.connection.connect;

import com.easysocket.config.DefaultX509ProtocolTrustManager;
import com.easysocket.config.SocketSSLConfig;
import com.easysocket.connection.action.SocketStatus;
import com.easysocket.entity.SocketAddress;
import com.easysocket.utils.LogUtil;
import com.easysocket.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Author：Alex
 * Date：2019/5/29
 * Note：tcp连接
 */
public class TcpConnection extends SuperConnection {
    /**
     * socket对象
     */
    private Socket socket;

    public TcpConnection(SocketAddress socketAddress) {
        super(socketAddress);
    }

    @Override
    protected void openConnection() throws Exception {
        try {
            socket = getSocket();
        } catch (Exception e) {
            e.printStackTrace();
            connectionStatus.set(SocketStatus.SOCKET_DISCONNECTED); // 设置为未连接
            throw new RuntimeException("创建socket失败");
        }

        // 进行socket连接
        socket.connect(new InetSocketAddress(socketAddress.getIp(), socketAddress.getPort()), socketOptions.getConnectTimeout());

        // 关闭Nagle算法,无论TCP数据报大小,立即发送
        socket.setTcpNoDelay(true);
        // 连接已经打开
        if (socket.isConnected() && !socket.isClosed()) {
            onConnectionOpened();
        }
    }

    @Override
    protected void closeConnection() throws IOException {
        if (socket != null)
            socket.close();
    }

    /**
     * 根据配置信息获取对应的socket
     *
     * @return
     */
    private synchronized Socket getSocket() throws Exception {
        // 自定义的socket生成工厂
        if (socketOptions.getSocketFactory() != null) {
            return socketOptions.getSocketFactory().createSocket(socketAddress, socketOptions);
        }
        // 默认操作
        SocketSSLConfig config = socketOptions.getEasySSLConfig();
        if (config == null) {
            return new Socket();
        }
        // 获取SSL配置工厂
        SSLSocketFactory factory = config.getCustomSSLFactory();
        if (factory == null) {
            String protocol = "SSL";
            if (!Utils.isStringEmpty(config.getProtocol())) {
                protocol = config.getProtocol();
            }

            TrustManager[] trustManagers = config.getTrustManagers();
            if (trustManagers == null || trustManagers.length == 0) {
                // 缺省信任所有证书
                trustManagers = new TrustManager[]{new DefaultX509ProtocolTrustManager()};
            }

            try {
                SSLContext sslContext = SSLContext.getInstance(protocol);
                sslContext.init(config.getKeyManagers(), trustManagers, new SecureRandom());
                return sslContext.getSocketFactory().createSocket();
            } catch (Exception e) {
                if (socketOptions.isDebug()) {
                    e.printStackTrace();
                }
                LogUtil.e(e.getMessage());
                return new Socket();
            }

        } else {
            try {
                return factory.createSocket();
            } catch (IOException e) {
                if (socketOptions.isDebug()) {
                    e.printStackTrace();
                }
                LogUtil.e(e.getMessage());
                return new Socket();
            }
        }
    }


    @Override
    public InputStream getInputStream() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            try {
                return socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public OutputStream getOutStream() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            try {
                return socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
