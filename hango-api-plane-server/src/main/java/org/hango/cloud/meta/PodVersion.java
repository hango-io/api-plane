package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class PodVersion {

    @NotEmpty(message = "clusterId")
    @JsonProperty(value = "ClusterId")
    private String clusterId;

    @NotEmpty(message = "namespace")
    @JsonProperty(value = "Namespace")
    private String namespace;

    @NotEmpty(message = "podnames")
    @JsonProperty(value = "PodNames")
    private List<String> podNames;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getPodNames() {
        return podNames;
    }

    public void setPodNames(List<String> podNames) {
        this.podNames = podNames;
    }
}
