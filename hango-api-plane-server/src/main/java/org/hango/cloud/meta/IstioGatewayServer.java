package org.hango.cloud.meta;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class IstioGatewayServer {

    /**
     * sever名称
     */
    private String name;
    /**
     * 端口协议
     */
    private String protocol;

    /**
     * 端口号
     */
    private Integer number;

    /**
     * 域名列表
     */
    private List<String> hosts;

    /**
     * tls配置
     */
    private IstioGatewayTLS istioGatewayTLS;

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

    public IstioGatewayTLS getIstioGatewayTLS() {
        return istioGatewayTLS;
    }

    public void setIstioGatewayTLS(IstioGatewayTLS istioGatewayTLS) {
        this.istioGatewayTLS = istioGatewayTLS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
