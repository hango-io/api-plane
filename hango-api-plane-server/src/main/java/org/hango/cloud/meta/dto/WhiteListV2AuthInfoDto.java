package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author wengyanghui | wengyanghui@corp.netease.com | 2020/03/23
 **/
public class WhiteListV2AuthInfoDto {

    @JsonProperty("Service")
    @NotNull(message = "service")
    private String service;

    @JsonProperty("DefaultPolicy")
    @NotNull(message = "default policy")
    private String defaultPolicy;

    /**
     * 当且仅当删除规则的时候使用这个字段
     */
    @JsonProperty("RuleName")
    private String ruleName;

    @JsonProperty("AuthRules")
    @NotNull(message = "auth rules")
    private List<WhiteListV2AuthRuleDto> authRules;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDefaultPolicy(String defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public List<WhiteListV2AuthRuleDto> getAuthRules() {
        return authRules;
    }

    public void setAuthRules(List<WhiteListV2AuthRuleDto> authRules) {
        this.authRules = authRules;
    }
}
