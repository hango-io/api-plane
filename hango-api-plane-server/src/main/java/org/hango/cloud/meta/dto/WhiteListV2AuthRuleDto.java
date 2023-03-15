package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;


public class WhiteListV2AuthRuleDto {

    @JsonProperty("RuleName")
    @NotNull(message = "rule name")
    private String ruleName;

    @JsonProperty("Enabled")
    @NotNull(message = "rule status")
    private Boolean enabled;

    @JsonProperty("MatchType")
    @NotNull(message = "match type")
    private String matchType;

    @JsonProperty("MatchApis")
    @NotNull(message = "match apis")
    private String matchApis;

    @JsonProperty("MatchConditions")
    @NotNull(message = "match conditions")
    private String matchConditions;

    public String getRuleName() {
        return ruleName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getMatchApis() {
        return matchApis;
    }

    public void setMatchApis(String matchApis) {
        this.matchApis = matchApis;
    }

    public String getMatchConditions() {
        return matchConditions;
    }

    public void setMatchConditions(String matchConditions) {
        this.matchConditions = matchConditions;
    }
}
