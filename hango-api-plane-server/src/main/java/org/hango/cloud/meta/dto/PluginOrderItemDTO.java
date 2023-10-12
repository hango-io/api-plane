package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hango.cloud.util.Const;

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
    private Object inline;

    @JsonProperty("rider")
    private Object rider;

    @JsonProperty("wasm")
    private Object wasm;

    @JsonProperty("listenerType")
    private String listenerType = Const.GATEWAY;


    public String getListenerType() {
        return listenerType;
    }

    public void setListenerType(String listenerType) {
        this.listenerType = listenerType;
    }

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

    public Object getRider() {
        return rider;
    }

    public void setRider(Object rider) {
        this.rider = rider;
    }

    public Object getWasm() {
        return wasm;
    }

    public void setWasm(Object wasm) {
        this.wasm = wasm;
    }
}
