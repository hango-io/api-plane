package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;

public class PortalIstioGatewayDTO {

    /**
     * 网关名称
     */
    @JsonProperty(value = "Name")
    @NotEmpty
    private String name;

    /**
     * 网关集群信息
     */
    @JsonProperty(value = "GwCluster")
    @NotEmpty
    private String gwCluster;

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

}
