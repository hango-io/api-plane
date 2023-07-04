package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * @Author zhufengwei
 * @Date 2023/7/3
 */
public class CustomPluginDTO {

    /** 插件的名称: uri-restriction */
    @NotEmpty(message = "PluginName can not be null")
    @JsonProperty("PluginName")
    private String pluginName;


    /** 插件的类型 wasm/lua */
    @NotEmpty(message = "Language can not be null")
    @Pattern(regexp = "wasm|lua", message = "Language must be wasm or lua")
    @JsonProperty("Language")
    private String language;

    /** 插件的内容 */
    @NotEmpty(message = "PluginContent can not be null")
    @JsonProperty("PluginContent")
    private String pluginContent;

    /** 插件schema */
    @NotEmpty(message = "Schema can not be null")
    @JsonProperty("Schema")
    private String schema;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPluginContent() {
        return pluginContent;
    }

    public void setPluginContent(String pluginContent) {
        this.pluginContent = pluginContent;
    }

}
