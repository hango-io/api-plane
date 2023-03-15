package org.hango.cloud.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import static org.hango.cloud.util.Const.VAILD_REGISTRY;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hango.cloud.util.Const.VAILD_REGISTRY;

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

    @Value("${registry:all}")
    private String registry;

    private final String ALL = "all";

    @Value("${ignorePlugins:#{null}}")
    private String ignorePlugins;

    public String getIgnorePlugins() {
        return ignorePlugins;
    }

    public Set<String> getIgnorePluginSet() {
        return StringUtils.isEmpty(ignorePlugins) ? Collections.emptySet() : new HashSet<>(Arrays.asList(ignorePlugins.split(",")));
    }


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

    public List<String> getRegistryList() {
        if (ALL.equals(registry)){
            return VAILD_REGISTRY;
        }
        return Arrays.asList(registry.split(","));
    }
}
