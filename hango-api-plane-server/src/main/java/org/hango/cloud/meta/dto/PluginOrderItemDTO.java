package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonProperty("inline")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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
