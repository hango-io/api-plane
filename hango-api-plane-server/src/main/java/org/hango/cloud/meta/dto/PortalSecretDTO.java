package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class PortalSecretDTO {
    /**
     * 证书名称
     */
    @JsonProperty(value = "Name")
    @NotEmpty
    private String name;

    /**
     * 服务端证书
     */
    @JsonProperty(value = "ServerCrt")
    private String serverCrt;

    /**
     * 服务端私钥
     */
    @JsonProperty(value = "ServerKey")
    private String serverKey;

    /**
     * 根证书
     */
    @JsonProperty(value = "CaCrt")
    private String caCrt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerCrt() {
        return serverCrt;
    }

    public void setServerCrt(String serverCrt) {
        this.serverCrt = serverCrt;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public String getCaCrt() {
        return caCrt;
    }

    public void setCaCrt(String caCrt) {
        this.caCrt = caCrt;
    }
}
