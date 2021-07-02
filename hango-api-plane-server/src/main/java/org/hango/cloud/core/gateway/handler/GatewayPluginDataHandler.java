package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.Gateway;
import org.hango.cloud.meta.GlobalPlugin;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GatewayPluginDataHandler implements DataHandler<GlobalPlugin> {

    List<FragmentWrapper> fragments;
    List<Gateway> gateways;
    String gatewayNamespace;

    public GatewayPluginDataHandler(List<FragmentWrapper> fragments, List<Gateway> gateways, String gatewayNamespace) {
        this.fragments = fragments;
        this.gateways = gateways;
        this.gatewayNamespace = gatewayNamespace;
    }

    @Override
    public List<TemplateParams> handle(GlobalPlugin gp) {

        List<String> plugins = extractFragments(fragments);
        TemplateParams pmParams = TemplateParams.instance()
                .put(TemplateConst.GATEWAY_PLUGIN_NAME, gp.getCode().toLowerCase())
                .put(TemplateConst.GATEWAY_PLUGIN_GATEWAYS, getGateways(gp).get(0))
                .put(TemplateConst.GATEWAY_PLUGIN_HOSTS, gp.getHosts())
                .put(TemplateConst.GATEWAY_PLUGIN_PLUGINS, plugins);
        return doHandle(pmParams);
    }

    List<TemplateParams> doHandle(TemplateParams params) {
        return Arrays.asList(params);
    }

    List<String> extractFragments(List<FragmentWrapper> fragments) {
        List<String> plugins = Collections.emptyList();
        if (!CollectionUtils.isEmpty(fragments)) {
            plugins = fragments.stream()
                    .filter(f -> f != null)
                    .map(f -> f.getContent())
                    .collect(Collectors.toList());
        }
        return plugins;
    }

    List<String> getGateways(GlobalPlugin gp) {
        if (gp.getGateway() == null) return Collections.emptyList();
        return Collections.singletonList(gp.getGateway());
    }

    private String getNamespace(String gateway) {
        final String gwClusgterKey = "gw_cluster";
        // 非k8s环境的网关可以根据label获取网关所在namespace
        final String namespaceKey = "gw_namespace";
        for (Gateway item : gateways) {
            if (Objects.nonNull(item.getLabels()) && Objects.equals(gateway, item.getLabels().get(gwClusgterKey))) {
                Pattern pattern = Pattern.compile("(.*?)\\.(.*?)\\.svc\\.cluster\\.(.*?)");
                Matcher matcher = pattern.matcher(item.getHostname());
                if (matcher.find()) {
                    return matcher.group(2);
                }
                // 使用gw_namespace指定的namespace
                if (item.getLabels().containsKey(namespaceKey)) {
                    return item.getLabels().get(namespaceKey);
                }
                throw new ApiPlaneException(String.format("The gateway [%s]`s hostname [%s] is not compliant", gateway, item.getHostname()));
            }
        }
        throw new ApiPlaneException(String.format("The gateway [%s] endpoint could not be found.", gateway));
    }
}
