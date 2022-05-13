package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
public class GlobalPluginDTO {

    @JsonProperty(value = "Gateway")
    @NotNull(message = "Gateway")
    private String gateway;

    @JsonProperty(value = "Hosts")
    @NotEmpty(message = "Hosts")
    private List<String> hosts;

    @JsonProperty(value = "Plugins")
    @NotEmpty(message = "Plugins")
    private List<String> plugins;

    @JsonProperty(value = "Code")
    @NotNull(message = "Code")
    private String code;

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
