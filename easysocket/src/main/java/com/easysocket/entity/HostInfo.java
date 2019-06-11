package com.easysocket.entity;

/**
 * Author：Alex
 * Date：2019/5/31
 * Note：主机地址相关信息
 */
public class HostInfo {

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
    private HostInfo backupInfo;

    /**
     * 获取备用的Ip和端口号
     *
     * @return 备用的端口号和IP地址
     */
    public HostInfo getBackupInfo() {
        return backupInfo;
    }

    /**
     * 设置备用的IP和端口号,可以不设置
     *
     * @param backupInfo 备用的IP和端口号信息
     */
    public void setBackupInfo(HostInfo backupInfo) {
        this.backupInfo = backupInfo;
    }

    public HostInfo(String ip, int port){
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
