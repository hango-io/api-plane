package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.GatewayPlugin;
import org.hango.cloud.util.constant.PluginConstant;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hango.cloud.core.template.TemplateConst.*;

/**
 * 路由插件CRD处理器
 *
 * @author yutao04
 * @date 2021.12.07
 **/
public class GatewayPluginConfigMapDataHandler implements DataHandler<GatewayPlugin> {

    private String sharedConfigName;
    private List<FragmentWrapper> fragments;
    private String configMapNamespace;

    public GatewayPluginConfigMapDataHandler(List<FragmentWrapper> fragments,
                                             String sharedConfigName,
                                             String configMapNamespace) {
        this.fragments = fragments;
        this.sharedConfigName = sharedConfigName;
        this.configMapNamespace = configMapNamespace;
    }

    @Override
    public List<TemplateParams> handle(GatewayPlugin plugin) {
        if (CollectionUtils.isEmpty(fragments)) {
            return Collections.emptyList();
        }

        List<String> descriptors = fragments.stream()
                .filter(Objects::nonNull)
                // 因配置迁移，模型有变，不再为数组
                .map(FragmentWrapper::getContent)
                .collect(Collectors.toList());
        TemplateParams configMapTempParams = TemplateParams.instance()
                .put(SHARED_CONFIG_NAME, sharedConfigName)
                .put(NAMESPACE, configMapNamespace)
                .put(SHARED_CONFIG_DESCRIPTOR, descriptors)
                .put(SERVICE_INFO_API_SERVICE, PluginConstant.DEFAULT_SERVICE_NAME)
                .put(SERVICE_INFO_API_GATEWAY, plugin.getGateway());

        if (plugin.isRoutePlugin()) {
            configMapTempParams.put(SERVICE_INFO_API_NAME, plugin.getRouteId());
        } else if (plugin.isGlobalPlugin()) {
            configMapTempParams.put(SERVICE_INFO_API_NAME, plugin.getCode());
        }
        return Arrays.asList(configMapTempParams);
    }
}
