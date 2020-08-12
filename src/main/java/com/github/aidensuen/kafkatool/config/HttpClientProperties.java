package com.github.aidensuen.kafkatool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
