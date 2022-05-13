package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.GatewayPlugin;
import org.hango.cloud.util.HandlerUtil;
import org.hango.cloud.util.constant.PluginConstant;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 路由插件CRD处理器
 *
 * @author yutao04
 * @date 2021.12.07
 **/
public class GatewayPluginDataHandler implements DataHandler<GatewayPlugin> {

    String gatewayNamespace;
    List<FragmentWrapper> fragments;

    public GatewayPluginDataHandler(List<FragmentWrapper> fragments, String gatewayNamespace) {
        this.fragments = fragments;
        this.gatewayNamespace = gatewayNamespace;
    }

    @Override
    public List<TemplateParams> handle(GatewayPlugin plugin) {
        if (plugin == null) {
            return Collections.EMPTY_LIST;
        }
        if (plugin.getPort() == null){
            plugin.setPort(80);
        }
        List<TemplateParams> params = new ArrayList<>();
        Map<String, List<String>> gatewayPluginMap = HandlerUtil.getGatewayPlugins(fragments);
        TemplateParams gatewayPluginParams = TemplateParams.instance()
                .put(TemplateConst.GATEWAY_PLUGIN_GATEWAYS, getGatewayName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_NAME, getGatewayPluginName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_PLUGINS, gatewayPluginMap);

        // 路由和全局插件模板渲染数据区分填充
        if (plugin.isRoutePlugin()) {
            gatewayPluginParams
                    .put(TemplateConst.GATEWAY_PLUGIN_ROUTES, getRouteList(plugin))
                    .put(TemplateConst.RESOURCE_IDENTITY, getIdentity(plugin))
                    .put(TemplateConst.SERVICE_INFO_API_SERVICE, PluginConstant.DEFAULT_SERVICE_NAME)
                    .put(TemplateConst.SERVICE_INFO_API_GATEWAY, plugin.getGateway())
                    .put(TemplateConst.SERVICE_INFO_API_NAME, plugin.getRouteId());
        } else if (plugin.isGlobalPlugin()) {
            if (!CollectionUtils.isEmpty(plugin.getHosts())){
                Integer port = plugin.getPort();
                List<String> hosts = plugin.getHosts().stream().map(host -> host + ":" + port).collect(Collectors.toList());
                plugin.setHosts(hosts);
            }
            gatewayPluginParams.put(TemplateConst.GATEWAY_PLUGIN_HOSTS, plugin.getHosts());
        }

        params.addAll(Arrays.asList((gatewayPluginParams)));

        return params;
    }

    private String getIdentity(GatewayPlugin plugin) {
        return String.format("%s-%s", plugin.getRouteId(), plugin.getGateway());
    }

    private List<String> getRouteList(GatewayPlugin plugin) {
        final String routeId = plugin.getRouteId();
        Integer port = plugin.getPort();
        return plugin.getHosts().stream()
                .map(host -> host + ":"+ port + "/" + routeId)
                .collect(Collectors.toList());
    }

    private List<String> getGatewayName(GatewayPlugin plugin) {
        return Collections.singletonList(String.format("%s/%s", gatewayNamespace, plugin.getGateway()));
    }

    private String getGatewayPluginName(GatewayPlugin plugin) {
        String pluginName = PluginConstant.DEFAULT_PLUGIN_NAME;

        if (plugin.isRoutePlugin()) {
            pluginName = plugin.getRouteId() + "-" + plugin.getGateway();
        } else if (plugin.isGlobalPlugin()) {
            pluginName = plugin.getCode().toLowerCase();
        }
        return pluginName;
    }
}
