package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PluginOrderDTO {

    /**
     * plugin manager 名称
     */
    @JsonProperty(value = "Name")
    private String name;


    @JsonProperty(value = "GwCluster")
    private String gwCluster;

    /**
     * 产品类型 apiGateway/ingressGateway
     */
    @JsonProperty(value = "GatewayKind")
    private String gatewayKind;

    /**
     * plm对应端口
     */
    @JsonProperty(value = "Port")
    private Integer port;


    @JsonProperty(value = "Plugins")
    private List<PluginOrderItemDTO> plugins;

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
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
