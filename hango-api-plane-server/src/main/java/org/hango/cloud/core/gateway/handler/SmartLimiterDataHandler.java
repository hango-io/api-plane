package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.GatewayPlugin;
import org.hango.cloud.util.HandlerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SmartLimiter资源处理器
 *
 * @author yutao04
 * @since 2022.09.01
 */
public class SmartLimiterDataHandler implements DataHandler<GatewayPlugin> {

    String gatewayNamespace;
    List<FragmentWrapper> fragments;

    public SmartLimiterDataHandler(List<FragmentWrapper> fragments, String gatewayNamespace) {
        this.fragments = fragments;
        this.gatewayNamespace = gatewayNamespace;
    }

    @Override
    public List<TemplateParams> handle(GatewayPlugin plugin) {
        if (plugin == null) {
            return Collections.EMPTY_LIST;
        }
        List<TemplateParams> params = new ArrayList<>();
        List<String> smartLimiters = HandlerUtil.getSmartLimiters(fragments);
        TemplateParams gatewayPluginParams = TemplateParams.instance()
                .put(TemplateConst.VERSION, plugin.getVersion())
                .put(TemplateConst.GATEWAY_PLUGIN_NAME, HandlerUtil.getGatewayPluginName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_PLUGINS, smartLimiters)
                .put(TemplateConst.GATEWAY_PLUGIN_NAMESPACE, gatewayNamespace);

        // 路由和全局插件模板渲染数据区分填充
        if (plugin.isRoutePlugin()) {
            gatewayPluginParams
                    .put(TemplateConst.GATEWAY_PLUGIN_ROUTE, HandlerUtil.getRoute(plugin));
        } else if (plugin.isGlobalPlugin()) {
            gatewayPluginParams.put(TemplateConst.GATEWAY_PLUGIN_HOSTS, HandlerUtil.completeHosts(plugin));
        }

        params.addAll(Arrays.asList((gatewayPluginParams)));

        return params;
    }
}
