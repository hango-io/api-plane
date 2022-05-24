package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public class PluginOrderDTO {

    @JsonProperty(value = "GatewayLabels")
    private Map<String, String> gatewayLabels;

    @NotEmpty(message = "plugins")
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
}
