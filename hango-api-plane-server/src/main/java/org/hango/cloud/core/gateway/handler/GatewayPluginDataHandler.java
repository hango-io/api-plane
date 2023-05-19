package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.GatewayPlugin;
import org.hango.cloud.util.HandlerUtil;
import org.hango.cloud.util.constant.PluginConstant;

import java.util.*;

/**
 * 路由插件CRD处理器
 *
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
                .put(TemplateConst.VERSION, plugin.getVersion())
                .put(TemplateConst.GATEWAY_PLUGIN_GATEWAYS, getGatewayName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_NAME, HandlerUtil.getGatewayPluginName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_PLUGINS, gatewayPluginMap);


        // 路由和全局插件模板渲染数据区分填充
        if (plugin.isRoutePlugin()) {
            gatewayPluginParams
                    .put(TemplateConst.GATEWAY_PLUGIN_ROUTE, HandlerUtil.getRoute(plugin))
                    .put(TemplateConst.SERVICE_INFO_API_SERVICE, PluginConstant.DEFAULT_SERVICE_NAME)
                    .put(TemplateConst.SERVICE_INFO_API_GATEWAY, plugin.getGateway())
                    .put(TemplateConst.SERVICE_INFO_API_NAME, plugin.getRouteId());
        } else if (plugin.isGlobalPlugin()) {
            gatewayPluginParams.put(TemplateConst.GATEWAY_PLUGIN_HOSTS, HandlerUtil.completeHosts(plugin));
        }

        params.addAll(Arrays.asList((gatewayPluginParams)));

        return params;
    }

    private List<String> getGatewayName(GatewayPlugin plugin) {
        return Collections.singletonList(String.format("%s/%s", gatewayNamespace, plugin.getGateway()));
    }
}
