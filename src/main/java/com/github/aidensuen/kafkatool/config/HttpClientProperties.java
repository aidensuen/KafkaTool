package com.github.aidensuen.kafkatool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "kafka.tool.remote")
public class HttpClientProperties {

    private Integer maxTotalConnect = 50;

    // Determines the maximum number of connections per host
    private Integer maxConnectPerRoute = 200;

    // Determines the timeout in milliseconds until a connection is established.
    private Integer connectTimeout = 30000;

    // The timeout when requesting a connection from the connection manager.
    private Integer requestTimeout = 30000;

    // The timeout for waiting for data
    private Integer socketTimeout = 60000;

    /**
     * Location of the trust store file.
     */
    private Resource trustStoreLocation;

    /**
     * Store password for the trust store file.
     */
    private char[] trustStorePassword;

    /**
     * Type of the trust store.
     */
    private String trustStoreType;

    public Integer getMaxTotalConnect() {
        return maxTotalConnect;
    }

    public void setMaxTotalConnect(Integer maxTotalConnect) {
        this.maxTotalConnect = maxTotalConnect;
    }

    public Integer getMaxConnectPerRoute() {
        return maxConnectPerRoute;
    }

    public void setMaxConnectPerRoute(Integer maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Resource getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public void setTrustStoreLocation(Resource trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    public char[] getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(char[] trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
}
