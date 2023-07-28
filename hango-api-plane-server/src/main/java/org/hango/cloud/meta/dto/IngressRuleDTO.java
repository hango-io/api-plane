package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/25
 */
public class IngressRuleDTO {
    /**
     * 域名
     */
    @JsonProperty("Host")
    private String host;


    /**
     * 域名
     */
    @JsonProperty("HTTPRules")
    private List<HTTPIngressPathDTO> httpRuleValueDTOS;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<HTTPIngressPathDTO> getHttpRuleValueDTOS() {
        return httpRuleValueDTOS;
    }

    public void setHttpRuleValueDTOS(List<HTTPIngressPathDTO> httpRuleValueDTOS) {
        this.httpRuleValueDTOS = httpRuleValueDTOS;
    }
}
