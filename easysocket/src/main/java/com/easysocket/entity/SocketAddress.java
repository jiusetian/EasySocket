package com.easysocket.entity;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：socket主机地址
 */
public class SocketAddress {

    /**
     * IPV4地址
     */
    private String ip;
    /**
     * 连接服务器端口号
     */
    private int port;
    /**
     * 当此IP地址Ping不通时的备用IP
     */
    private SocketAddress backupAddress;

    /**
     * 获取备用的Ip和端口号
     *
     * @return 备用的端口号和IP地址
     */
    public SocketAddress getBackupAddress() {
        return backupAddress;
    }

    /**
     * 设置备用的IP和端口号,可以不设置
     *
     * @param backupAddress 备用的IP和端口号信息
     */
    public void setBackupAddress(SocketAddress backupAddress) {
        this.backupAddress = backupAddress;
    }

    public SocketAddress(String ip, int port){
        this.ip =ip;
        this.port =port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
