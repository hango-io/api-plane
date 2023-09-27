package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2023/9/26
 */
public class PluginStatusDTO {
    /**
     * plugin manager 名称
     */
    @JsonProperty(value = "PluginManagerName")
    private String pluginManagerName;

    /**
     * plugin 名称
     */
    @JsonProperty(value = "PluginName")
    private String pluginName;

    /**
     * 插件状态
     */
    @JsonProperty(value = "Enable")
    private Boolean enable;

    public String getPluginManagerName() {
        return pluginManagerName;
    }

    public void setPluginManagerName(String pluginManagerName) {
        this.pluginManagerName = pluginManagerName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
