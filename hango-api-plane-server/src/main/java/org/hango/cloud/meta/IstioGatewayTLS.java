package org.hango.cloud.meta;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class IstioGatewayTLS {
    private String mode;

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
