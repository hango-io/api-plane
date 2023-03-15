package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @author xin li
 * @date 2022/5/13 14:31
 */
public class EnvoyFilterItemDTO {
    @JsonProperty("applyTo")
    @NotNull(message = "applyTo")
    private String applyTo;
    @JsonProperty("match")
    @NotNull(message = "match")
    private Object match;
    @JsonProperty("patch")
    @NotNull(message = "patch")
    private Object patch;

    public String getApplyTo() {
        return applyTo;
    }

    public void setApplyTo(String applyTo) {
        this.applyTo = applyTo;
    }

    public Object getMatch() {
        return match;
    }

    public void setMatch(Object match) {
        this.match = match;
    }

    public Object getPatch() {
        return patch;
    }

    public void setPatch(Object patch) {
        this.patch = patch;
    }
}
