package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import istio.networking.v1alpha3.SidecarOuterClass;

import java.util.List;

/**
 * EnvoyFilter dto
 * 同yml格式
 *
 * @author xin li
 * @date 2022/5/13 14:29
 */
public class EnvoyFilterDTO {

    @JsonProperty("PortNumber")
    private int portNumber;
    @JsonProperty("Namespace")
    private String namespace;

    @JsonProperty("ConfigPatches")
    private List<String> configPatches;

    @JsonProperty(value = "WorkloadSelector")
    private SidecarOuterClass.WorkloadSelector workloadSelector;

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getConfigPatches() {
        return configPatches;
    }

    public void setConfigPatches(List<String> configPatches) {
        this.configPatches = configPatches;
    }

    public SidecarOuterClass.WorkloadSelector getWorkloadSelector() {
        return workloadSelector;
    }

    public void setWorkloadSelector(SidecarOuterClass.WorkloadSelector workloadSelector) {
        this.workloadSelector = workloadSelector;
    }
}
