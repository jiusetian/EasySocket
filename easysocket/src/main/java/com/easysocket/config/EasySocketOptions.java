package com.easysocket.config;

import com.easysocket.connection.reconnect.AbsReconnection;
import com.easysocket.connection.reconnect.DefaultReConnection;
import com.easysocket.entity.sender.SuperClientHeart;
import com.easysocket.interfaces.io.IReaderProtocol;

import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：socket相关配置
 */
public class EasySocketOptions {

    /**
     * 框架是否是调试模式
     */
    private static boolean isDebug = true;
    /**
     * 写入Socket管道的字节序
     */
    private ByteOrder writeOrder;
    /**
     * 从Socket读取字节时的字节序
     */
    private ByteOrder readOrder;
    /**
     * 从socket读取数据时遵从数据包结构协议，在业务层进行定义
     */
    private IReaderProtocol readerProtocol;
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
    private EasySocketFactory socketFactory;
    /**
     * 获取请求消息唯一标识singer的工厂，默认为null
     */
    private CallbackSingerFactory callbackSingerFactory;

    /**
     * 请求超时时间，单位毫秒
     */
    private long requestTimeout;
    /**
     * 是否开启请求超时检测
     */
    private boolean isOpenRequestTimeout;
    /**
     * 客户端心跳包
     */
    private SuperClientHeart clientHeart;
    /**
     * 是否开启心跳功能，默认关闭
     */
    private boolean isActiveHeart;
    /**
     * 是否启动socket的智能分发功能，即回调功能，默认关闭
     */
    private boolean isActiveResponseDispatch;


    public boolean isDebug() {
        return isDebug;
    }

    /**
     * 静态内部类
     */
    public static class Builder {
        EasySocketOptions socketOptions;

        //首先获得一个默认的配置
        public Builder() {
            this(getDefaultOptions());
        }

        public Builder(EasySocketOptions defaultOptions) {
            socketOptions = defaultOptions;
        }

        /**
         * 设置是否启动回调功能，默认是关闭的
         *
         * @param activeResponseDispatch
         * @return
         */
        public Builder setActiveResponseDispatch(boolean activeResponseDispatch) {
            socketOptions.isActiveResponseDispatch = activeResponseDispatch;
            return this;
        }

        /**
         * 设置是否开启心跳，默认开启
         *
         * @param activeHeart
         * @return
         */
        public Builder setActiveHeart(boolean activeHeart) {
            socketOptions.isActiveHeart = activeHeart;
            return this;
        }

        /**
         * 设置要发送的客户端心跳包实例，只有设置了客户端心跳包实例，才能开启心跳管理器的自动(发送/接收)心跳包的功能
         *
         * @param clientHeart
         * @return
         */
        public Builder setClientHeart(SuperClientHeart clientHeart) {
            socketOptions.clientHeart = clientHeart;
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
         * @param callbackSingerFactory
         */
        public Builder setCallbackSingerFactory(CallbackSingerFactory callbackSingerFactory) {
            socketOptions.callbackSingerFactory = callbackSingerFactory;
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
        public Builder setReaderProtocol(IReaderProtocol readerProtocol) {
            socketOptions.readerProtocol = readerProtocol;
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
        public Builder setSocketFactory(EasySocketFactory socketFactory) {
            socketOptions.socketFactory = socketFactory;
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
        options.heartbeatFreq = 5 * 1000;
        options.readerProtocol = new DefaultReaderProtocol();
        options.maxResponseDataMb = 5;
        options.connectTimeout = 5 * 1000; //连接超时默认5秒
        options.maxWriteBytes = 100;
        options.maxReadBytes = 50;
        options.readOrder = ByteOrder.BIG_ENDIAN;
        options.writeOrder = ByteOrder.BIG_ENDIAN;
        options.maxHeartbeatLoseTimes = 5;
        options.reconnectionManager = new DefaultReConnection();
        options.easySSLConfig = null;
        options.socketFactory = null;
        options.callbackSingerFactory = null;
        options.requestTimeout = 10 * 1000; //默认十秒
        options.isOpenRequestTimeout = true; //默认开启
        options.clientHeart = null;
        options.isActiveHeart = false; //默认开启心跳包检测连接状态
        options.isActiveResponseDispatch = false; //默认关闭回调功能
        return options;
    }

    public ByteOrder getWriteOrder() {
        return writeOrder;
    }

    public ByteOrder getReadOrder() {
        return readOrder;
    }

    public IReaderProtocol getReaderProtocol() {
        return readerProtocol;
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

    public EasySocketFactory getSocketFactory() {
        return socketFactory;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public boolean isOpenRequestTimeout() {
        return isOpenRequestTimeout;
    }

    public CallbackSingerFactory getCallbackSingerFactory() {
        return callbackSingerFactory;
    }

    public SuperClientHeart getClientHeart() {
        return clientHeart;
    }

    public boolean isActiveHeart() {
        return isActiveHeart;
    }

    public boolean isActiveResponseDispatch() {
        return isActiveResponseDispatch;
    }

    /** 各种set方法*/

    public static void setIsDebug(boolean isDebug) {
        EasySocketOptions.isDebug = isDebug;
    }

    public void setWriteOrder(ByteOrder writeOrder) {
        this.writeOrder = writeOrder;
    }

    public void setReadOrder(ByteOrder readOrder) {
        this.readOrder = readOrder;
    }

    public void setReaderProtocol(IReaderProtocol readerProtocol) {
        this.readerProtocol = readerProtocol;
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

    public void setSocketFactory(EasySocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public void setCallbackSingerFactory(CallbackSingerFactory callbackSingerFactory) {
        this.callbackSingerFactory = callbackSingerFactory;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public void setOpenRequestTimeout(boolean openRequestTimeout) {
        isOpenRequestTimeout = openRequestTimeout;
    }

    public void setClientHeart(SuperClientHeart clientHeart) {
        this.clientHeart = clientHeart;
    }

    public void setActiveHeart(boolean activeHeart) {
        isActiveHeart = activeHeart;
    }

    public void setActiveResponseDispatch(boolean activeResponseDispatch) {
        isActiveResponseDispatch = activeResponseDispatch;
    }

}
