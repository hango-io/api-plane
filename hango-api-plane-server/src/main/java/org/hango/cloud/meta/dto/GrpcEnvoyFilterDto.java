package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xin li
 * @date 2022/5/20 11:16
 */
public class GrpcEnvoyFilterDto {
    /**
     * 网关的名称空间
     */
    @JsonProperty(value = "Namespace")
    private String namespace;

    /**
     * 网关
     */
    @JsonProperty(value = "GwCluster")
    private String gwCluster;
    /**
     * 网关的端口号
     */
    @JsonProperty(value = "PortNumber")
    private int portNumber;

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


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

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

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
}
