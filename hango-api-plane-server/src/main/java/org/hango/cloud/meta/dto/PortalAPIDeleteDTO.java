package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class PortalAPIDeleteDTO {

    @NotEmpty(message = "gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    /**
     * api唯一标识
     */
    @NotEmpty(message = "api code")
    @JsonProperty(value = "Code")
    private String code;

    /**
     * 插件
     */
    @JsonProperty(value = "Plugins")
    private List<String> plugins;

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }
}
