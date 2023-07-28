package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.meta.CRDMetaEnum;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


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

    /**
     * meta数据传输集
     * Map<mata_type,meta_data>
     * mata_type meta类型
     *
     * @see CRDMetaEnum
     * mata_type: 服务meta数据类型
     * meta_data: 服务meta<label_key,label_value>
     */
    @JsonProperty(value = "MetaMap")
    private Map<String, Map<String,String>> metaMap;

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

    public Map<String, Map<String, String>> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, Map<String, String>> metaMap) {
        this.metaMap = metaMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
