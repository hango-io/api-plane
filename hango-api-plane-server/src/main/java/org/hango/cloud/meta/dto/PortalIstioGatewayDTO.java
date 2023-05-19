package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

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

    /**
     * server配置
     */
    @JsonProperty(value = "Servers")
    private List<PortalIstioGatewayServerDTO> servers;

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

    public List<PortalIstioGatewayServerDTO> getServers() {
        return servers;
    }

    public void setServers(List<PortalIstioGatewayServerDTO> servers) {
        this.servers = servers;
    }
}
