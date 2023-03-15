package org.hango.cloud.meta;


import istio.networking.v1alpha3.SidecarOuterClass;

import java.util.List;

/**
 * @author xin li
 * @date 2022/5/16 10:09
 */
public class EnvoyFilterOrder {

    private int portNumber;
    private String namespace;

    private List<String> configPatches;

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
