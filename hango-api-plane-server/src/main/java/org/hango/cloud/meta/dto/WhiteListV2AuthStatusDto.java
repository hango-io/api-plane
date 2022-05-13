package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author wengyanghui | wengyanghui@corp.netease.com | 2020/03/23
 **/
public class WhiteListV2AuthStatusDto {

    @JsonProperty("Service")
    @NotNull(message = "service")
    private String service;

    @JsonProperty("AuthOn")
    @NotNull(message = "auth status")
    private Boolean authOn;

    @JsonProperty("DefaultPolicy")
    @NotNull(message = "default policy")
    private String defaultPolicy;

    @JsonProperty("AuthRules")
    @NotNull(message = "auth rules")
    private List<WhiteListV2AuthRuleDto> authRules;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Boolean getAuthOn() {
        return authOn;
    }

    public void setAuthOn(Boolean authOn) {
        this.authOn = authOn;
    }

    public String getDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDefaultPolicy(String defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public List<WhiteListV2AuthRuleDto> getAuthRules() {
        return authRules;
    }

    public void setAuthRules(List<WhiteListV2AuthRuleDto> authRules) {
        this.authRules = authRules;
    }
}
