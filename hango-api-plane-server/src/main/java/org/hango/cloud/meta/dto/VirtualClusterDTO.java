package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.List;

/**
 * VirtualCluster DTO
 * VirtualCluster资源配置
 * @author hanjiahao
 */
public class VirtualClusterDTO {
    /**
     * Virtual_Cluster Name
     */
    @JsonProperty(value = "Name")
    private String virtualClusterName;

    /**
     * VirtualCluster headers
     */
    @JsonProperty(value = "Headers")
    @Valid
    private List<PairMatchDTO> headers;

    public List<PairMatchDTO> getHeaders() {
        return headers;
    }

    public void setHeaders(List<PairMatchDTO> headers) {
        this.headers = headers;
    }

    public String getVirtualClusterName() {
        return virtualClusterName;
    }

    public void setVirtualClusterName(String virtualClusterName) {
        this.virtualClusterName = virtualClusterName;
    }
}
