package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/23
 **/
public class ServiceSubsetDTO {

    @JsonProperty(value = "Name")
    @NotEmpty(message = "subset name")
    private String name;

    @JsonProperty(value = "Labels")
    private Map<String, String> labels;

    @Valid
    @JsonProperty(value = "TrafficPolicy")
    private PortalTrafficPolicyDTO trafficPolicy;

    @JsonProperty(value = "StaticAddrList")
    private List<String> staticAddrs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public PortalTrafficPolicyDTO getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(PortalTrafficPolicyDTO trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public List<String> getStaticAddrs() {
        return staticAddrs;
    }

    public void setStaticAddrs(List<String> staticAddrs) {
        this.staticAddrs = staticAddrs;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
