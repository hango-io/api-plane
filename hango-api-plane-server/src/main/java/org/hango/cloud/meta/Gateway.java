package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public class Gateway {

//    @JsonProperty(value = "Name")
    @JsonIgnore
    private String hostname;

    @JsonProperty(value = "Address")
    private String address;

    @JsonProperty(value = "Labels")
    private Map<String, String> labels;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gateway gateway = (Gateway) o;
        return Objects.equals(hostname, gateway.hostname) &&
                Objects.equals(address, gateway.address) &&
                Objects.equals(labels, gateway.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, address, labels);
    }
}
