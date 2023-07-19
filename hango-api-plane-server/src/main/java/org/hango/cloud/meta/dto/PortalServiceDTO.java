package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hango.cloud.meta.CRDMetaEnum;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortalServiceDTO {

    /**
     * 服务唯一标识
     */
    @NotEmpty(message = "code")
    @JsonProperty(value = "Code")
    private String code;

    /**
     * 对应后端服务
     */
    @NotEmpty(message = "backend service")
    @JsonProperty(value = "BackendService")
    private String backendService;

    /**
     * 类型
     */
    @NotEmpty(message = "type")
    @JsonProperty(value = "Type")
    @Pattern(regexp = "(STATIC|DYNAMIC)", message = "type")
    private String type;

    /**
     * 权重
     */
    @JsonProperty(value = "Weight")
    private Integer weight;

    @JsonProperty(value = "Gateway")
    private String gateway;

    @JsonProperty(value = "Protocol")
    @Pattern(regexp = "(http|https|grpc|tcp|udp)", message = "protocol")
    private String protocol = "http";

    @JsonProperty(value = "TrafficPolicy")
    @Valid
    private PortalTrafficPolicyDTO trafficPolicy;

    @NotEmpty(message = "service tag")
    @JsonProperty(value = "ServiceTag")
    private String serviceTag;

    @JsonProperty(value = "Subsets")
    @Valid
    private List<ServiceSubsetDTO> subsets;

    @JsonProperty(value = "Version")
    private Long version;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public List<ServiceSubsetDTO> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<ServiceSubsetDTO> subsets) {
        this.subsets = subsets;
    }

    public PortalTrafficPolicyDTO getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(PortalTrafficPolicyDTO trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Map<String, Map<String, String>> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, Map<String, String>> metaMap) {
        this.metaMap = metaMap;
    }
}
