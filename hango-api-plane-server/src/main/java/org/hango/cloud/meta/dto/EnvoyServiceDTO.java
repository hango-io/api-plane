package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/13
 */
public class EnvoyServiceDTO {

    @NotNull
    @JsonProperty("GwClusterName")
    private String gwClusterName;

    @JsonProperty("ServiceType")
    private String serviceType;


    @JsonProperty("Ports")
    private List<EnvoyServicePortDTO> ports;

    @JsonProperty("Ips")
    private List<String> ips;

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public List<EnvoyServicePortDTO> getPorts() {
        return ports;
    }

    public void setPorts(List<EnvoyServicePortDTO> ports) {
        this.ports = ports;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
