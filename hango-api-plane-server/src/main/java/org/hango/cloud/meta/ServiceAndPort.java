package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/12/13
 **/
public class ServiceAndPort {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Port")
    private List<Integer> port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPort() {
        return port;
    }

    public void setPort(List<Integer> port) {
        this.port = port;
    }
}
