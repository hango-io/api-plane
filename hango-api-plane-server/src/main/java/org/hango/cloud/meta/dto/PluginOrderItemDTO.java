package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class PluginOrderItemDTO {

    @JsonProperty("enable")
    @NotNull(message = "enable")
    private Boolean enable;

    @JsonProperty("name")
    @NotNull(message = "name")
    private String name;

    @JsonProperty("settings")
    private Object settings;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getSettings() {
        return settings;
    }

    public void setSettings(Object settings) {
        this.settings = settings;
    }
}
