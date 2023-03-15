package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class PortalIstioGatewayServerDTO {
    /**
     * 端口协议
     */
    @JsonProperty(value = "Protocol")
    @NotEmpty
    private String protocol;

    /**
     * 端口号
     */
    @JsonProperty(value = "Number")
    @NotEmpty
    private Integer number;

    /**
     * 域名列表
     */
    @JsonProperty(value = "Hosts")
    private List<String> hosts;

    /**
     * tls配置
     */
    @JsonProperty(value = "TLSSettings")
    private PortalIstioGatewayTLSDTO portalIstioGatewayTLSDTO;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public PortalIstioGatewayTLSDTO getPortalIstioGatewayTLSDTO() {
        return portalIstioGatewayTLSDTO;
    }

    public void setPortalIstioGatewayTLSDTO(PortalIstioGatewayTLSDTO portalIstioGatewayTLSDTO) {
        this.portalIstioGatewayTLSDTO = portalIstioGatewayTLSDTO;
    }
}
