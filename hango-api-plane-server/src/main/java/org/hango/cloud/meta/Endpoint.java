package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint {

    @JsonProperty(value = "Name")
    private String hostname;

    @JsonProperty(value = "Address")
    private String address;

    @JsonProperty(value = "Port")
    private Integer port;

    @JsonProperty(value = "Protocol")
    private String protocol;

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getLabel(String key) {
        if (labels == null || StringUtils.isBlank(key)){
            return StringUtils.EMPTY;
        }
        return labels.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(hostname, endpoint.hostname) &&
                Objects.equals(address, endpoint.address) &&
                Objects.equals(port, endpoint.port) &&
                Objects.equals(protocol, endpoint.protocol) &&
                Objects.equals(labels, endpoint.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, address, port, protocol, labels);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
