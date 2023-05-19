package org.hango.cloud.meta;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @link template/plugin/manager/plugin-support-config.json
 * @date 2022/11/22
 */
public class PluginSupportConfig {

    /**
     * 网关类型
     */
    private String gatewayKind;

    /**
     * 插件支持列表
     */
    private List<PluginSupportDetail> plugins;

    public String getGatewayKind() {
        return gatewayKind;
    }

    public void setGatewayKind(String gatewayKind) {
        this.gatewayKind = gatewayKind;
    }

    public List<PluginSupportDetail> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginSupportDetail> plugins) {
        this.plugins = plugins;
    }
}
