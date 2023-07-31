package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @Author zhufengwei
 * @Date 2023/7/3
 */
public class CustomPluginDTO {

    /** 插件的名称: uri-restriction.lua */
    @NotEmpty(message = "PluginName can not be null")
    @JsonProperty("PluginName")
    private String pluginName;


    /** 插件的内容 */
    @NotEmpty(message = "PluginContent can not be null")
    @JsonProperty("PluginContent")
    private String pluginContent;

    /** 网关标识 */
    @NotEmpty(message = "GwCluster can not be null")
    @JsonProperty("GwCluster")
    private String gwCluster;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginContent() {
        return pluginContent;
    }

    public void setPluginContent(String pluginContent) {
        this.pluginContent = pluginContent;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }
}
