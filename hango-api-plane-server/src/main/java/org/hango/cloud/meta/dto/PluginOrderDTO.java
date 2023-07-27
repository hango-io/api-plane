package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class PluginOrderDTO {

    /**
     * plugin manager 名称
     */
    @JsonProperty(value = "Name")
    private String name;

    /**
     * plugin manager 名称
     */
    @JsonProperty(value = "GatewayKind")
    private String gatewayKind;

    /**
     * plm对应端口
     */
    @JsonProperty(value = "Port")
    private Integer port;

    @JsonProperty(value = "GatewayLabels")
    private Map<String, String> gatewayLabels;

    @JsonProperty(value = "Plugins")
    private List<PluginOrderItemDTO> plugins;

    public Map<String, String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Map<String, String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

    public List<PluginOrderItemDTO> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginOrderItemDTO> plugins) {
        this.plugins = plugins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGatewayKind() {
        return gatewayKind;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setGatewayKind(String gatewayKind) {
        this.gatewayKind = gatewayKind;
    }
}
