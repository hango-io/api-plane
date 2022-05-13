package org.hango.cloud.meta;

import org.hango.cloud.util.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.List;

/**
 * 网关插件实体类，承载着插件CRD相关的信息（目前插件CRD包括VirtualService和GatewayPlugin）
 *
 * @author yutao04
 * @date 2021.12.06
 */
public class GatewayPlugin {

    private List<String> plugins;

    private String routeId;

    private String pluginType;

    private List<String> hosts;

    private String gateway;

    private String code;

    private Integer port;

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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGateway() {
        return gateway;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    /**
     * 根据路由标识判断该插件是否为路由级别插件
     *
     * @return 是否为路由级别插件（true: 路由级别；false: 非路由级别）
     */
    public boolean isRoutePlugin() {
        return StringUtils.isNotEmpty(routeId);
    }

    /**
     * 根据项目标识判断该插件是否为全局（项目级别）插件
     *
     * @return 是否为全局插件（true: 全局级别；false: 非全局级别）
     */
    public boolean isGlobalPlugin() {
        return StringUtils.isNotEmpty(code);
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 打印插件配置
     *
     * @param logger 日志打印实例
     */
    public void showPluginConfigsInLog(Logger logger) {
        if (logger == null) {
            return;
        }
        for (int i = 1; i <= plugins.size(); i++) {
            logger.info("{} plugin config count: {}, plugin NO.{}: {}",
                    LogConstant.PLUGIN_LOG_NOTE,
                    plugins.size(),
                    i,
                    plugins.get(i - 1));
        }
    }


}
