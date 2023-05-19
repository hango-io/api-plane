package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/30 16:55
 **/
public class ResourceCheckDTO {

    /**
     * 需要校验的网关资源
     */
    @JsonProperty(value = "Resource")
    Map<String, List<ResourceDTO>> resource;

    /**
     * 需要校验的网关code(gwClusterName + "-" + virtualGatewayName)
     */
    @JsonProperty(value = "Gateway")
    String gateway;

    public Map<String, List<ResourceDTO>> getResource() {
        return resource;
    }

    public void setResource(Map<String, List<ResourceDTO>> resource) {
        this.resource = resource;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
