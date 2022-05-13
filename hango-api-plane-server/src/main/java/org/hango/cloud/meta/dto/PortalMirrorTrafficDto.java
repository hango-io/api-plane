package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * 流量镜像配置
 * @Author Jin Chenxi jinchenxi01@corp.netease.com 2021/6/11
 */
public class PortalMirrorTrafficDto {

    /**
     * 对应后端服务
     */
    @NotEmpty(message = "BackendService")
    @JsonProperty(value = "BackendService")
    private String backendService;

    /**
     * 流量镜像转发端口
     */
    @NotEmpty(message = "Port")
    @JsonProperty(value = "Port")
    private int port;

    /**
     * 版本信息
     */
    @JsonProperty(value = "Subset")
    private String subset;

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }
}
