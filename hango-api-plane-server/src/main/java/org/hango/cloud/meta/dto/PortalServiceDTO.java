package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
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
    @Pattern(regexp = "(http|https|grpc)", message = "protocol")
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
}
