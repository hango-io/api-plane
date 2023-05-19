package org.hango.cloud.meta;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class Secret {
    /**
     * 证书名称
     */
    private String name;

    /**
     * 服务端证书
     */
    private String serverCrt;

    /**
     * 服务端私钥
     */
    private String serverKey;

    /**
     * 根证书
     */
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
