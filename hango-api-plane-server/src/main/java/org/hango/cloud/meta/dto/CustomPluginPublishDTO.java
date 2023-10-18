package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2023/9/26
 */
public class CustomPluginPublishDTO {
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
     * plugin 端口
     */
    @JsonProperty(value = "Port")
    private Integer port;

    /**
     * wasm插件
     */
    @JsonProperty(value = "Wasm")
    private RiderDTO wasm;

    /**
     * lua插件
     */
    @JsonProperty(value = "Lua")
    private RiderDTO lua;

    /**
     * 插件分类 trafficPolicy（流量管理）、auth(认证鉴权)  security(安全)、dataFormat（数据转换）
     */
    @JsonProperty(value = "PluginCategory")
    private String pluginCategory;


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

    public String getPluginCategory() {
        return pluginCategory;
    }

    public void setPluginCategory(String pluginCategory) {
        this.pluginCategory = pluginCategory;
    }

    public RiderDTO getWasm() {
        return wasm;
    }

    public void setWasm(RiderDTO wasm) {
        this.wasm = wasm;
    }

    public RiderDTO getLua() {
        return lua;
    }

    public void setLua(RiderDTO lua) {
        this.lua = lua;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
