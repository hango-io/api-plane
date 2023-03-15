package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class PortalIstioGatewayTLSDTO {
    /**
     * TLS认证方式 SIMPLE/MUTUAL
     */
    @JsonProperty(value = "Mode")
    private String mode;

    /**
     * secret名称
     */
    @JsonProperty(value = "CredentialName")
    private String credentialName;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }
}
