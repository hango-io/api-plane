package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 资源详情
 */
public class ResourceDTO {

    /**
     * 资源Id，例如serviceId,routeID等
     */
    @JsonProperty("ResourceId")
    private Long resourceId;

    /**
     * 资源名称，例如dynamic-116-prod-gateway
     */
    @JsonProperty("ResourceName")
    private String resourceName;

    /**
     * 资源版本号，通过version校验配置是否下发
     */
    @JsonProperty("ResourceVersion")
    private String resourceVersion;


    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }


    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }


    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
