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

    @Value("${ldsPort:80}")
    private String ldsPort;

    public String getResourceNamespace() {
        return resourceNamespace;
    }

    public String getApiPlaneType() {
        return apiPlaneType;
    }

    public String getApiPlaneVersion() {
        return apiPlaneVersion;
    }

    public String getLdsPort() {
        return ldsPort;
    }

}
