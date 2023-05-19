package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author xin li
 * @date 2022/5/20 11:16
 */
public class GrpcEnvoyFilterDTO extends EnvoyFilterDTO{

    /**
     * pb文件
     */
    @JsonProperty(value = "ProtoDescriptorBin")
    private String protoDescriptorBin;

    /**
     * 要支持协议转换的services
     */
    @JsonProperty(value = "Services")
    private List<String> services;

    public String getProtoDescriptorBin() {
        return protoDescriptorBin;
    }

    public void setProtoDescriptorBin(String protoDescriptorBin) {
        this.protoDescriptorBin = protoDescriptorBin;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

}
