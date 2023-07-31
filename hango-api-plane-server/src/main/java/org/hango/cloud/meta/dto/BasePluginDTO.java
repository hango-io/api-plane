package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2023/8/10
 */
public class BasePluginDTO {
    /**
     * plugin manager name
     */
    @JsonProperty(value = "Name")
    private String name;

    /**
     * 插件名称
     */
    @JsonProperty(value = "PluginType")
    private String pluginType;


    /**
     * 插件配置
     */
    @JsonProperty(value = "PluginConfig")
    private String pluginConfig;

    /**
     * 插件语言 lua/wasm/inline
     */
    @JsonProperty(value = "Language")
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(String pluginConfig) {
        this.pluginConfig = pluginConfig;
    }
}
