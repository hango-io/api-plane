package org.hango.cloud.meta;

import org.hango.cloud.meta.dto.PortalServiceConnectionPoolDTO;

import java.util.List;


public class Service extends CommonModel {

    private String code;

    /**
     * 对应后端服务
     */
    private String backendService;

    /**
     * 类型
     */
    private String type;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 所属网关
     */
    private String gateway;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 连续错误数
     */
    private Integer consecutiveErrors;

    /**
     * 基础驱逐时间
     */
    private Long baseEjectionTime;

    /**
     * 最大驱逐比例
     */
    private Integer maxEjectionPercent;

    /**
     * 最小健康比例
     */
    private Integer minHealthPercent;

    /**
     * 健康检查路径
     */
    private String path;

    /**
     * 健康检查超时时间
     */
    private Long timeout;

    /**
     * 期望响应码
     */
    private List<Integer> expectedStatuses;

    /**
     * 健康实例检查间隔
     */
    private Long healthyInterval;

    /**
     * 健康阈值
     */
    private Integer healthyThreshold;

    /**
     * 异常实例检查间隔
     */
    private Long unhealthyInterval;

    /**
     * 异常实例阈值
     */
    private Integer unhealthyThreshold;

    /**
     * 服务标签，唯一
     */
    private String serviceTag;

    /**
     * 负载均衡
     */
    private ServiceLoadBalancer loadBalancer;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 服务subsets
     */
    private List<ServiceSubset> subsets;

    private String subset;

    /**
     * 连接池
     */
    private PortalServiceConnectionPoolDTO connectionPool;

    /**
     * tcp连接池
     */
    private PortalServiceConnectionPoolDTO.PortalServiceTcpConnectionPoolDTO tcpConnectionPool;

    /**
     * 最大连接数
     */
    private Integer maxConnections;

    /**
     * tcp连接超时时间
     */
    private String connectTimeout;

    /**
     * http连接池
     */
    private PortalServiceConnectionPoolDTO.PortalServiceHttpConnectionPoolDTO httpConnectionPool;

    /**
     * 最大等待HTTP请求数。默认值是1024，仅适用于HTTP/1.1的服务，因为HTTP/2协议的请求在到来时
     * 会立即复用连接，不会在连接池等待
     */
    private Integer http1MaxPendingRequests;

    /**
     * 最大请求数。默认值是1024，仅使用于HTTP/2的服务。HTTP/1.1的服务使用maxConnections即可
     */
    private Integer http2MaxRequests;

    /**
     * 每个连接的最大请求数。HTTP/1.1和HTTP/2连接池都遵循此参数，如果没有设置则没有限制，如果设置
     * 为1则表示禁用了keep-alive，0表示不限制最多处理的请求数为2^29
     */
    private Integer maxRequestsPerConnection;

    /**
     * 空闲超时，定义在多长时间内没有活动请求则关闭连接
     */
    private Integer idleTimeout;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public Long getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(Long baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public Integer getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(Integer maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public Integer getMinHealthPercent() {
        return minHealthPercent;
    }

    public void setMinHealthPercent(Integer minHealthPercent) {
        this.minHealthPercent = minHealthPercent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public List<Integer> getExpectedStatuses() {
        return expectedStatuses;
    }

    public void setExpectedStatuses(List<Integer> expectedStatuses) {
        this.expectedStatuses = expectedStatuses;
    }

    public Long getHealthyInterval() {
        return healthyInterval;
    }

    public void setHealthyInterval(Long healthyInterval) {
        this.healthyInterval = healthyInterval;
    }

    public Integer getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(Integer healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    public Long getUnhealthyInterval() {
        return unhealthyInterval;
    }

    public void setUnhealthyInterval(Long unhealthyInterval) {
        this.unhealthyInterval = unhealthyInterval;
    }

    public Integer getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public ServiceLoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(ServiceLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<ServiceSubset> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<ServiceSubset> subsets) {
        this.subsets = subsets;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public PortalServiceConnectionPoolDTO getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(PortalServiceConnectionPoolDTO connectionPool) {
        this.connectionPool = connectionPool;
    }

    public PortalServiceConnectionPoolDTO.PortalServiceTcpConnectionPoolDTO getTcpConnectionPool() {
        return tcpConnectionPool;
    }

    public void setTcpConnectionPool(PortalServiceConnectionPoolDTO.PortalServiceTcpConnectionPoolDTO tcpConnectionPool) {
        this.tcpConnectionPool = tcpConnectionPool;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public PortalServiceConnectionPoolDTO.PortalServiceHttpConnectionPoolDTO getHttpConnectionPool() {
        return httpConnectionPool;
    }

    public void setHttpConnectionPool(PortalServiceConnectionPoolDTO.PortalServiceHttpConnectionPoolDTO httpConnectionPool) {
        this.httpConnectionPool = httpConnectionPool;
    }

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

    public static class ServiceLoadBalancer {
        private String simple;
        private ConsistentHash consistentHash;

        public String getSimple() {
            return simple;
        }

        public void setSimple(String simple) {
            this.simple = simple;
        }

        public ConsistentHash getConsistentHash() {
            return consistentHash;
        }

        public void setConsistentHash(ConsistentHash consistentHash) {
            this.consistentHash = consistentHash;
        }

        public static class ConsistentHash {

            private String httpHeaderName;
            private Boolean useSourceIp;
            private HttpCookie httpCookie;

            public static class HttpCookie {
                private String name;
                private String path;
                private Integer ttl;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getPath() {
                    return path;
                }

                public void setPath(String path) {
                    this.path = path;
                }

                public Integer getTtl() {
                    return ttl;
                }

                public void setTtl(Integer ttl) {
                    this.ttl = ttl;
                }
            }

            public String getHttpHeaderName() {
                return httpHeaderName;
            }

            public void setHttpHeaderName(String httpHeaderName) {
                this.httpHeaderName = httpHeaderName;
            }

            public Boolean getUseSourceIp() {
                return useSourceIp;
            }

            public void setUseSourceIp(Boolean useSourceIp) {
                this.useSourceIp = useSourceIp;
            }

            public HttpCookie getHttpCookie() {
                return httpCookie;
            }

            public void setHttpCookie(HttpCookie httpCookie) {
                this.httpCookie = httpCookie;
            }
        }

    }
}
