package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.util.HandlerUtil;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * api级别的全局插件
 **/
public class ApiGatewayPluginDataHandler extends APIDataHandler {

    String gatewayNamespace;
    List<FragmentWrapper> fragments;
    String ldsPort;

    public ApiGatewayPluginDataHandler(List<FragmentWrapper> fragments, String gatewayNamespace, String ldsPort) {
        this.fragments = fragments;
        this.gatewayNamespace = gatewayNamespace;
        this.ldsPort = ldsPort;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, API api) {

        if (api == null) return Collections.EMPTY_LIST;
        Map<String, List<String>> apiPlugins = HandlerUtil.getApiPlugins(fragments);
        List<String> gateways = api.getGateways();
        List<TemplateParams> params = new ArrayList<>();
        List<String> routes = api.getHosts().stream()
                .map(h -> h +":" + ldsPort + "/" + api.getName())
                .collect(Collectors.toList());

        gateways.forEach(gw -> {
            TemplateParams pmParams = TemplateParams.instance()
                    .setParent(tp)
                    .put(TemplateConst.GATEWAY_PLUGIN_NAME, getGatewayPluginName(gw, api.getName()))
                    .put(TemplateConst.RESOURCE_IDENTITY, getIdentity(api.getName(), gw))
                    .put(TemplateConst.GATEWAY_PLUGIN_GATEWAYS, gw)
                    .put(TemplateConst.GATEWAY_PLUGIN_ROUTES, routes)
                    .put(TemplateConst.GATEWAY_PLUGIN_PLUGINS, apiPlugins);

            params.addAll(doHandle(pmParams));
        });

        return params;
    }

    public String getGatewayPluginName(String gateway, String api) {

        List<String> parts = new ArrayList<>();

        if (!StringUtils.isEmpty(api)) {
            parts.add(api);
        }
        if (!StringUtils.isEmpty(gateway)) {
            parts.add(gateway);
        }

        if (CollectionUtils.isEmpty(parts)) return null;
        return String.join("-", parts);
    }

    public String getIdentity(String api, String gw) {
        return String.format("%s-%s", api, gw);
    }

    List<TemplateParams> doHandle(TemplateParams params) {
        return Arrays.asList(params);
    }

}
