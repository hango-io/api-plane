package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/25
 */
public class IngressDTO {
    /**
     * 名称
     */
    @JsonProperty("Name")
    private String name;

    /**
     * http协议端口
     */
    @JsonProperty("Port")
    private Integer port;

    /**
     * http协议端口
     */
    @JsonProperty("TlsPort")
    private Integer tlsPort;

    /**
     * 命名空间
     */
    @JsonProperty("Namespace")
    private String namespace;


    /**
     * 所属项目id
     */
    @JsonProperty("ProjectCode")
    private String projectCode;

    /**
     * 所属项目id
     */
    @JsonProperty("IngressRule")
    private List<IngressRuleDTO> ingressRuleDTOS;


    /**
     * 配置详细内容
     */
    @JsonProperty("Content")
    private String content;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public List<IngressRuleDTO> getIngressRuleDTOS() {
        return ingressRuleDTOS;
    }

    public void setIngressRuleDTOS(List<IngressRuleDTO> ingressRuleDTOS) {
        this.ingressRuleDTOS = ingressRuleDTOS;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getTlsPort() {
        return tlsPort;
    }

    public void setTlsPort(Integer tlsPort) {
        this.tlsPort = tlsPort;
    }
}
