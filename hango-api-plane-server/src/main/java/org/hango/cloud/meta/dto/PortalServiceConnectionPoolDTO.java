package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 连接池配置
 *
 */
public class PortalServiceConnectionPoolDTO implements Serializable {
    /**
     * TCP连接池
     */
    @JsonProperty(value = "TCP")
    private PortalServiceTcpConnectionPoolDTO tcp;

    /**
     * HTTP连接池
     */
    @JsonProperty(value = "HTTP")
    private PortalServiceHttpConnectionPoolDTO http;

    public PortalServiceTcpConnectionPoolDTO getTcp() {
        return tcp;
    }

    public void setTcp(PortalServiceTcpConnectionPoolDTO tcp) {
        this.tcp = tcp;
    }

    public PortalServiceHttpConnectionPoolDTO getHttp() {
        return http;
    }

    public void setHttp(PortalServiceHttpConnectionPoolDTO http) {
        this.http = http;
    }

    public static class PortalServiceHttpConnectionPoolDTO implements Serializable{

        /**
         * 最大等待HTTP请求数。默认值是1024，仅适用于HTTP/1.1的服务，因为HTTP/2协议的请求在到来时
         * 会立即复用连接，不会在连接池等待
         */
        @JsonProperty(value = "Http1MaxPendingRequests")
        private Integer http1MaxPendingRequests;

        /**
         * 最大请求数。默认值是1024，仅使用于HTTP/2的服务。HTTP/1.1的服务使用maxConnections即可
         */
        @JsonProperty(value = "Http2MaxRequests")
        private Integer http2MaxRequests;

        /**
         * 每个连接的最大请求数。HTTP/1.1和HTTP/2连接池都遵循此参数，如果没有设置则没有限制，如果设置
         * 为1则表示禁用了keep-alive，0表示不限制最多处理的请求数为2^29
         */
        @JsonProperty(value = "MaxRequestsPerConnection")
        private Integer maxRequestsPerConnection;

        /**
         * 空闲超时，定义在多长时间内没有活动请求则关闭连接
         */
        @JsonProperty(value = "IdleTimeout")
        private Integer idleTimeout;

        public Integer getHttp1MaxPendingRequests() {
            return http1MaxPendingRequests;
        }

        public void setHttp1MaxPendingRequests(Integer http1MaxPendingRequests) {
            this.http1MaxPendingRequests = http1MaxPendingRequests;
        }

        public Integer getHttp2MaxRequests() {
            return http2MaxRequests;
        }

        public void setHttp2MaxRequests(Integer http2MaxRequests) {
            this.http2MaxRequests = http2MaxRequests;
        }

        public Integer getMaxRequestsPerConnection() {
            return maxRequestsPerConnection;
        }

        public void setMaxRequestsPerConnection(Integer maxRequestsPerConnection) {
            this.maxRequestsPerConnection = maxRequestsPerConnection;
        }

        public Integer getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Integer idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class PortalServiceTcpConnectionPoolDTO implements Serializable {
        /**
         * 最大连接数
         */
        @JsonProperty(value = "MaxConnections")
        private Integer maxConnections;

        /**
         * tcp连接超时时间
         */
        @JsonProperty(value = "ConnectTimeout")
        private Integer connectTimeout;

        public Integer getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
        }

        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
