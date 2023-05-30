package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2023/5/25
 * ingress dto
 * - backend:
 *     serviceName: httpbin
 *     servicePort: 80
 *   path: /status
 *   pathType: Prefix
 */
public class HTTPIngressPathDTO {
    /**
     * path
     */
    @JsonProperty("Path")
    private String path;

    /**
     * 匹配方式
     */
    @JsonProperty("PathType")
    private String pathType;

    /**
     * 名称
     */
    @JsonProperty("ServiceName")
    private String serviceName;

    /**
     * 名称
     */
    @JsonProperty("ServicePort")
    private Integer servicePort;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathType() {
        return pathType;
    }

    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getServicePort() {
        return servicePort;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }
}
