package org.hango.cloud.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {

    @Value("${resourceNamespace:gateway-system}")
    private String resourceNamespace;

    @Value("${apiPlaneType}")
    private String apiPlaneType;

    @Value("${apiPlaneVersion}")
    private String apiPlaneVersion;

    @Value("${istioRev:gw-1.12}")
    private String istioRev;

    @Value("${telnet.connect.timeout:3000}")
    private Integer telnetConnectTimeout;

    @Value("${customDefaultRespCode:500}")
    private int customDefaultRespCode;

    public String getResourceNamespace() {
        return resourceNamespace;
    }

    public String getApiPlaneType() {
        return apiPlaneType;
    }

    public String getIstioRev() {
        return istioRev;
    }

    public String getApiPlaneVersion() {
        return apiPlaneVersion;
    }

    public Integer getTelnetConnectTimeout() {
        return telnetConnectTimeout;
    }

    public int getCustomDefaultRespCode() {
        return customDefaultRespCode;
    }
}
