package org.hango.cloud.meta;

import java.util.List;

/**
 * 网关插件实体类，承载着插件CRD相关的信息（目前插件CRD包括VirtualService和GatewayPlugin）
 *
 *
 * @date 2021.12.06
 */
public class GatewayPlugin {

    private List<String> plugins;


    private List<String> hosts;

    private String gateway;

    private String gwCluster;

    private String code;

    private Integer port;

    private Long version;

    private String pluginScope;


    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }


    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGateway() {
        return gateway;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }



    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

}
