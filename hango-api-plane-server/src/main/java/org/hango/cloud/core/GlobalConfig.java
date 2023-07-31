package org.hango.cloud.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static org.hango.cloud.util.Const.VAILD_REGISTRY;

@Configuration
public class GlobalConfig {

    @Value("${resourceNamespace:gateway-system}")
    private String resourceNamespace;

    @Value("${istioRev:gw-stable}")
    private String istioRev;

    @Value("${telnet.connect.timeout:3000}")
    private Integer telnetConnectTimeout;

    @Value("${customDefaultRespCode:500}")
    private int customDefaultRespCode;

    @Value("${registry:all}")
    private String registry;

    //项目隔离标识
    @Value("${projectCode:skiff.netease.com/project}")
    private String projectCode;

    //ingress controller标识，和istio保持一致
    @Value("${ingressClass:hango}")
    private String ingressClass;

    //自定义插件代码挂载cm 名称
    @Value("${pluginContentConfigName:hango-rider-plugin}")
    private String pluginContentConfigName;



    private final String ALL = "all";

    @Value("${ignorePlugins:#{null}}")
    private String ignorePlugins;


    public Set<String> getIgnorePluginSet() {
        return StringUtils.isEmpty(ignorePlugins) ? Collections.emptySet() : new HashSet<>(Arrays.asList(ignorePlugins.split(",")));
    }


    public String getResourceNamespace() {
        return resourceNamespace;
    }


    public String getIstioRev() {
        return istioRev;
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

    public String getProjectCode() {
        return projectCode;
    }

    public String getIngressClass() {
        return ingressClass;
    }

    public String getPluginContentConfigName() {
        return pluginContentConfigName;
    }


}
