package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ApiOption {

    @JsonProperty(value = "ExtractMethod")
    private Boolean extractMethod;
    /**
     * 负载均衡
     */
    @JsonProperty(value = "LoadBalancer")
    private String loadBalancer;

    /**
     * 请求是否幂等
     */
    @JsonProperty(value = "Idempotent")
    private Boolean Idempotent;

    /**
     * 保留原始host
     */
    @JsonProperty(value = "PreserveHost")
    private Boolean preserveHost = true;

    /**
     * 重试次数
     */
    @JsonProperty(value = "Retries")
    private Integer retries = 5;

    @JsonProperty(value = "ConnectTimeout")
    private Long connectTimeout;

    /**
     * 上下游超时时间(发送、读取)
     */
    @JsonProperty(value = "IdleTimeout")
    private Long IdleTimeout;

    @JsonProperty(value = "HttpsOnly")
    private Boolean httpsOnly;

    @JsonProperty(value = "HttpIfTerminated")
    private Boolean httpIfTerminated;

    public Boolean getExtractMethod() {
        return extractMethod;
    }

    public void setExtractMethod(Boolean extractMethod) {
        this.extractMethod = extractMethod;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Boolean getIdempotent() {
        return Idempotent;
    }

    public void setIdempotent(Boolean idempotent) {
        Idempotent = idempotent;
    }

    public Boolean getPreserveHost() {
        return preserveHost;
    }

    public void setPreserveHost(Boolean preserveHost) {
        this.preserveHost = preserveHost;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getIdleTimeout() {
        return IdleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        IdleTimeout = idleTimeout;
    }

    public Boolean getHttpsOnly() {
        return httpsOnly;
    }

    public void setHttpsOnly(Boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    public Boolean getHttpIfTerminated() {
        return httpIfTerminated;
    }

    public void setHttpIfTerminated(Boolean httpIfTerminated) {
        this.httpIfTerminated = httpIfTerminated;
    }
}
