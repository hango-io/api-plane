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

    @JsonProperty("port")
    @NotNull(message = "port")
    private Integer port;

    @JsonProperty("settings")
    private Object settings;

    @JsonProperty("inline")
    private Object inline;

    @JsonProperty("listenerType")
    private Integer listenerType;

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Object getInline() {
        return inline;
    }

    public void setInline(Object inline) {
        this.inline = inline;
    }

    public Integer getListenerType() {
        return listenerType;
    }

    public void setListenerType(Integer listenerType) {
        this.listenerType = listenerType;
    }
}
