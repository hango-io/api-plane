package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * EnvoyFilter dto
 * 同yml格式
 *
 * @author xin li
 * @date 2022/5/13 14:29
 */
public class EnvoyFilterDTO {
    /**
     * filter名称
     */
    @JsonProperty(value = "Name")
    private String name;

    /**
     * 网关
     */
    @JsonProperty(value = "GwCluster")
    private String gwCluster;
    /**
     * 网关的端口号
     */
    @JsonProperty(value = "PortNumber")
    private int portNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
}
