package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public class GatewaySync {

    @JsonProperty(value = "Name")
    private String name;

    @JsonProperty(value = "IsSync")
    private Boolean isSync;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSync() {
        return isSync;
    }

    public void setSync(Boolean sync) {
        isSync = sync;
    }
}
