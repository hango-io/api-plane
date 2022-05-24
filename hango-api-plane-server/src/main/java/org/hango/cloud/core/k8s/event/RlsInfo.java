package org.hango.cloud.core.k8s.event;

import java.util.Map;

public class RlsInfo {

    Map<String, String> labels;
    String clusterId;
    String namespace;
    String key;
    String val;

    public RlsInfo(Map<String, String> labels, String clusterId, String namespace, String key, String val) {
        this.labels = labels;
        this.clusterId = clusterId;
        this.namespace = namespace;
        this.key = key;
        this.val = val;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "RlsInfo{" +
                "labels=" + labels +
                ", clusterId='" + clusterId + '\'' +
                ", namespace='" + namespace + '\'' +
                ", key='" + key + '\'' +
                ", val='" + val + '\'' +
                '}';
    }
}
