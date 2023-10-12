package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 *
 * @date 2021.12.07
 *
 * 插件实体类
 */
public class GatewayPluginDTO {

    @JsonProperty(value = "Plugins")
    private List<String> plugins;

    @JsonProperty(value = "PluginType")
    private String pluginType;

    @JsonProperty(value = "Hosts")
    private List<String> hosts;

    @NotEmpty(message = "Gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    @JsonProperty(value = "Code")
    private String code;

    @JsonProperty(value = "Port")
    private Integer port;

    @JsonProperty(value = "Version")
    private Long version;

    @JsonProperty(value = "PluginScope")
    private String pluginScope;

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

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


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

}
