package org.hango.cloud.meta;

import java.util.List;
import java.util.Map;


public class PluginOrder extends CommonModel {

    private Map<String, String> gatewayLabels;

    private List<String> plugins;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Map<String, String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }
}
