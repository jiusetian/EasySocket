package com.easysocket.config;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * socket的ssl配置
 */

public class SocketSSLConfig {
    /**
     * 安全协议名称(缺省为SSL)
     */
    private String mProtocol;
    /**
     * 信任证书管理器(缺省为X509)
     */
    private TrustManager[] mTrustManagers;
    /**
     * 证书秘钥管理器(缺省为null)
     */
    private KeyManager[] mKeyManagers;
    /**
     * 自定义SSLFactory(缺省为null)
     */
    private SSLSocketFactory mCustomSSLFactory;

    private SocketSSLConfig() {

    }

    public static class Builder {

        private SocketSSLConfig mConfig;

        public Builder() {
            mConfig = new SocketSSLConfig();
        }

        public Builder setProtocol(String protocol) {
            mConfig.mProtocol = protocol;
            return this;
        }

        public Builder setTrustManagers(TrustManager[] trustManagers) {
            mConfig.mTrustManagers = trustManagers;
            return this;
        }

        public Builder setKeyManagers(KeyManager[] keyManagers) {
            mConfig.mKeyManagers = keyManagers;
            return this;
        }

        public Builder setCustomSSLFactory(SSLSocketFactory customSSLFactory) {
            mConfig.mCustomSSLFactory = customSSLFactory;
            return this;
        }

        public SocketSSLConfig build() {
            return mConfig;
        }
    }

    public KeyManager[] getKeyManagers() {
        return mKeyManagers;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public TrustManager[] getTrustManagers() {
        return mTrustManagers;
    }

    public SSLSocketFactory getCustomSSLFactory() {
        return mCustomSSLFactory;
    }
}
