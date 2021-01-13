package com.easysocket.config;

import com.easysocket.connection.reconnect.AbsReconnection;
import com.easysocket.connection.reconnect.DefaultReConnection;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.config.IMessageProtocol;

import java.nio.ByteOrder;

/**
 * Author：Alex。
 * Date：2019/5/31。
 * Note：socket相关配置。
 */
public class EasySocketOptions {

    /**
     * 是否调试模式
     */
    private static boolean isDebug = true;
    /**
     * 主机地址
     */
    private SocketAddress socketAddress;
    /**
     * 备用主机地址
     */
    private SocketAddress backupAddress;
    /**
     * 写入Socket管道的字节序
     */
    private ByteOrder writeOrder;
    /**
     * 从Socket读取字节时的字节序
     */
    private ByteOrder readOrder;
    /**
     * 从socket读取数据时遵从的数据包结构协议，在业务层进行定义
     */
    private IMessageProtocol messageProtocol;
    /**
     * 写数据时单个数据包的最大值
     */
    private int maxWriteBytes;
    /**
     * 读数据时单次读取最大缓存值，数值越大效率越高，但是系统消耗也越大
     */
    private int maxReadBytes;
    /**
     * 心跳频率/毫秒
     */
    private long heartbeatFreq;
    /**
     * 心跳最大的丢失次数，大于这个数据，将断开socket连接
     */
    private int maxHeartbeatLoseTimes;
    /**
     * 连接超时时间(毫秒)
     */
    private int connectTimeout;
    /**
     * 服务器返回数据的最大值（单位Mb），防止客户端内存溢出
     */
    private int maxResponseDataMb;
    /**
     * socket重连管理器
     */
    private AbsReconnection reconnectionManager;
    /**
     * 安全套接字相关配置
     */
    private SocketSSLConfig easySSLConfig;
    /**
     * socket工厂
     */
    private SocketFactory socketFactory;
    /**
     * 实现回调功能需要callbackID，而callbackID是保存在发送消息和应答消息中的，此工厂用来获取socket消息中
     * 保存callbackID值的key，比如json格式中的key-value中的key
     */
    private CallbackIDFactory callbackIDFactory;
    /**
     * 请求超时时间，单位毫秒
     */
    private long requestTimeout;
    /**
     * 是否开启请求超时检测
     */
    private boolean isOpenRequestTimeout;

    /**
     * IO字符流的编码方式，默认utf-8
     */
    private String charsetName;

    public boolean isDebug() {
        return isDebug;
    }


    /**
     * 静态内部类
     */
    public static class Builder {
        EasySocketOptions socketOptions;

        // 首先获得一个默认的配置
        public Builder() {
            this(getDefaultOptions());
        }

        public Builder(EasySocketOptions defaultOptions) {
            socketOptions = defaultOptions;
        }

        /**
         * 设置socket 主机地址
         *
         * @param socketAddress
         * @return
         */
        public Builder setSocketAddress(SocketAddress socketAddress) {
            socketOptions.socketAddress = socketAddress;
            return this;
        }

        /**
         * 设置备用的主机地址
         *
         * @param backupAddress
         * @return
         */
        public Builder setBackupAddress(SocketAddress backupAddress) {
            socketOptions.backupAddress = backupAddress;
            return this;
        }

        /**
         * 设置是否开启请求超时的检测
         *
         * @param openRequestTimeout
         * @return
         */
        public Builder setOpenRequestTimeout(boolean openRequestTimeout) {
            socketOptions.isOpenRequestTimeout = openRequestTimeout;
            return this;
        }

        /**
         * 设置请求超时时间
         *
         * @param requestTimeout 毫秒
         * @return
         */
        public Builder setRequestTimeout(long requestTimeout) {
            socketOptions.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * 设置请求ack的工厂
         *
         * @param callbackIDFactory
         */
        public Builder setCallbackIDFactory(CallbackIDFactory callbackIDFactory) {
            socketOptions.callbackIDFactory = callbackIDFactory;
            return this;
        }


        /**
         * 设置写数据的字节顺序
         *
         * @param writeOrder
         * @return
         */
        public Builder setWriteOrder(ByteOrder writeOrder) {
            socketOptions.writeOrder = writeOrder;
            return this;
        }

        /**
         * 设置读数据的字节顺序
         *
         * @param readOrder
         * @return
         */
        public Builder setReadOrder(ByteOrder readOrder) {
            socketOptions.readOrder = readOrder;
            return this;
        }

        /**
         * 设置读取数据的数据结构协议
         *
         * @param readerProtocol
         * @return
         */
        public Builder setReaderProtocol(IMessageProtocol readerProtocol) {
            socketOptions.messageProtocol = readerProtocol;
            return this;
        }

        /**
         * 设置写数据时单个数据包的最大值
         *
         * @param maxWriteBytes
         * @return
         */
        public Builder setMaxWriteBytes(int maxWriteBytes) {
            socketOptions.maxWriteBytes = maxWriteBytes;
            return this;
        }

        /**
         * 设置读数据时单次读取的最大缓存值
         *
         * @param maxReadBytes
         * @return
         */
        public Builder setMaxReadBytes(int maxReadBytes) {
            socketOptions.maxReadBytes = maxReadBytes;
            return this;
        }

        /**
         * 设置心跳发送频率，单位毫秒
         *
         * @param heartbeatFreq
         * @return
         */
        public Builder setHeartbeatFreq(long heartbeatFreq) {
            socketOptions.heartbeatFreq = heartbeatFreq;
            return this;
        }

        /**
         * 设置心跳丢失的最大允许数，如果超过这个最大数就断开socket连接
         *
         * @param maxHeartbeatLoseTimes
         * @return
         */
        public Builder setMaxHeartbeatLoseTimes(int maxHeartbeatLoseTimes) {
            socketOptions.maxHeartbeatLoseTimes = maxHeartbeatLoseTimes;
            return this;
        }

        /**
         * 设置连接超时时间
         *
         * @param connectTimeout
         * @return
         */
        public Builder setConnectTimeout(int connectTimeout) {
            socketOptions.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * 设置服务器返回数据的允许的最大值，单位兆
         *
         * @param maxResponseDataMb
         * @return
         */
        public Builder setMaxResponseDataMb(int maxResponseDataMb) {
            socketOptions.maxResponseDataMb = maxResponseDataMb;
            return this;
        }

        /**
         * 设置重连管理器
         *
         * @param reconnectionManager
         * @return
         */
        public Builder setReconnectionManager(AbsReconnection reconnectionManager) {
            socketOptions.reconnectionManager = reconnectionManager;
            return this;
        }

        /**
         * 安全套接字的配置
         *
         * @param easySSLConfig
         * @return
         */
        public Builder setEasySSLConfig(SocketSSLConfig easySSLConfig) {
            socketOptions.easySSLConfig = easySSLConfig;
            return this;
        }

        /**
         * 自定义创建socket工厂
         *
         * @param socketFactory
         * @return
         */
        public Builder setSocketFactory(SocketFactory socketFactory) {
            socketOptions.socketFactory = socketFactory;
            return this;
        }

        public Builder setCharsetName(String charsetName) {
            socketOptions.charsetName = charsetName;
            return this;
        }

        public EasySocketOptions build() {
            return socketOptions;
        }
    }


    /**
     * 获取默认的配置
     *
     * @return
     */
    public static EasySocketOptions getDefaultOptions() {
        EasySocketOptions options = new EasySocketOptions();
        options.socketAddress = null;
        options.backupAddress = null;
        options.heartbeatFreq = 5 * 1000;
        options.messageProtocol = null;
        options.maxResponseDataMb = 5;
        options.connectTimeout = 5 * 1000; // 连接超时默认5秒
        options.maxWriteBytes = 100;
        options.maxReadBytes = 50;
        options.readOrder = ByteOrder.BIG_ENDIAN;
        options.writeOrder = ByteOrder.BIG_ENDIAN;
        options.maxHeartbeatLoseTimes = 5;
        options.reconnectionManager = new DefaultReConnection();
        options.easySSLConfig = null;
        options.socketFactory = null;
        options.callbackIDFactory = null;
        options.requestTimeout = 10 * 1000; // 默认十秒
        options.isOpenRequestTimeout = true; // 默认开启
        options.charsetName = "UTF-8";
        return options;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public ByteOrder getWriteOrder() {
        return writeOrder;
    }

    public ByteOrder getReadOrder() {
        return readOrder;
    }

    public IMessageProtocol getMessageProtocol() {
        return messageProtocol;
    }

    public int getMaxWriteBytes() {
        return maxWriteBytes;
    }

    public int getMaxReadBytes() {
        return maxReadBytes;
    }

    public long getHeartbeatFreq() {
        return heartbeatFreq;
    }

    public int getMaxHeartbeatLoseTimes() {
        return maxHeartbeatLoseTimes;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getMaxResponseDataMb() {
        return maxResponseDataMb;
    }

    public AbsReconnection getReconnectionManager() {
        return reconnectionManager;
    }

    public SocketSSLConfig getEasySSLConfig() {
        return easySSLConfig;
    }

    public SocketFactory getSocketFactory() {
        return socketFactory;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public boolean isOpenRequestTimeout() {
        return isOpenRequestTimeout;
    }

    public CallbackIDFactory getCallbackIDFactory() {
        return callbackIDFactory;
    }

    public static void setIsDebug(boolean isDebug) {
        EasySocketOptions.isDebug = isDebug;
    }

    public void setWriteOrder(ByteOrder writeOrder) {
        this.writeOrder = writeOrder;
    }

    public void setReadOrder(ByteOrder readOrder) {
        this.readOrder = readOrder;
    }

    public void setMessageProtocol(IMessageProtocol messageProtocol) {
        this.messageProtocol = messageProtocol;
    }

    public void setMaxWriteBytes(int maxWriteBytes) {
        this.maxWriteBytes = maxWriteBytes;
    }

    public void setMaxReadBytes(int maxReadBytes) {
        this.maxReadBytes = maxReadBytes;
    }

    public void setHeartbeatFreq(long heartbeatFreq) {
        this.heartbeatFreq = heartbeatFreq;
    }

    public void setMaxHeartbeatLoseTimes(int maxHeartbeatLoseTimes) {
        this.maxHeartbeatLoseTimes = maxHeartbeatLoseTimes;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setMaxResponseDataMb(int maxResponseDataMb) {
        this.maxResponseDataMb = maxResponseDataMb;
    }

    public void setReconnectionManager(AbsReconnection reconnectionManager) {
        this.reconnectionManager = reconnectionManager;
    }

    public void setEasySSLConfig(SocketSSLConfig easySSLConfig) {
        this.easySSLConfig = easySSLConfig;
    }

    public void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public void setCallbackIDFactory(CallbackIDFactory callbackIDFactory) {
        this.callbackIDFactory = callbackIDFactory;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public void setOpenRequestTimeout(boolean openRequestTimeout) {
        isOpenRequestTimeout = openRequestTimeout;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public SocketAddress getBackupAddress() {
        return backupAddress;
    }

}
