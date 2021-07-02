package org.hango.cloud.core.gateway.handler;


import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.gateway.processor.ModelProcessor;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.util.HandlerUtil;
import org.hango.cloud.util.PriorityUtil;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;

import java.util.*;
import java.util.stream.Collectors;

public class BaseVirtualServiceAPIDataHandler extends APIDataHandler {

    static final String apiVirtualServiceMatch = "gateway/api/virtualServiceMatch";
    static final String apiVirtualServiceExtra = "gateway/api/virtualServiceExtra";
    static final String apiVirtualServiceRoute = "gateway/api/virtualServiceRoute";
    static final String apiVirtualServiceHttpRetry = "gateway/api/virtualServiceHttpRetry";
    static final String apiVirtualServiceMeta = "gateway/api/virtualServiceMeta";
    static final String apiVirtualServiceMatchPriority = "gateway/api/virtualServiceMatchPriority";

    ModelProcessor subModelProcessor;
    List<FragmentWrapper> fragments;
    List<Endpoint> endpoints;
    boolean simple;

    public BaseVirtualServiceAPIDataHandler(ModelProcessor modelProcessor) {
        this.subModelProcessor = modelProcessor;
    }

    public BaseVirtualServiceAPIDataHandler(ModelProcessor subModelProcessor, List<FragmentWrapper> fragments, List<Endpoint> endpoints, boolean simple) {
        this.subModelProcessor = subModelProcessor;
        this.fragments = fragments;
        this.endpoints = endpoints;
        this.simple = simple;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {

        // 插件分为match、api、host三个级别
        List<String> matchPlugins = new ArrayList<>();
        // api下的插件 可以根据插件划分
        Map<String, List<String>> apiPlugins = new HashMap<>();
        List<String> hostPlugins = new ArrayList<>();

        HandlerUtil.distributePlugins(fragments, matchPlugins, apiPlugins, hostPlugins);

        int pluginPriority = calculatePluginPriority(api, baseParams.get(TemplateConst.VIRTUAL_SERVICE_MATCH_PRIORITY));

        String matchYaml = produceMatch(baseParams);
        String httpRetryYaml = produceHttpRetry(baseParams);
        String matchPriorityYaml = produceMatchPriority(baseParams);

        TemplateParams vsParams = TemplateParams.instance()
                .setParent(baseParams)
                .put(TemplateConst.VIRTUAL_SERVICE_MATCH_YAML, matchYaml)
                .put(TemplateConst.VIRTUAL_SERVICE_MATCH_PRIORITY_YAML, matchPriorityYaml)
                .put(TemplateConst.API_MATCH_PLUGINS, matchPlugins)
                .put(TemplateConst.VIRTUAL_SERVICE_HTTP_RETRY_YAML, httpRetryYaml)
                .put(TemplateConst.VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY, pluginPriority)
                .put(TemplateConst.SERVICE_INFO_VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY, pluginPriority);

        List<TemplateParams> collect = api.getGateways().stream()
                .map(gw -> {
                    String subset = buildVirtualServiceSubsetName(api.getService(), api.getName(), gw);
                    String route = produceRoute(api, endpoints, subset);

                    TemplateParams tmpParams = TemplateParams.instance()
                            .setParent(vsParams)
                            .put(TemplateConst.GATEWAY_NAME, buildGatewayName(api.getService(), gw))
                            .put(TemplateConst.VIRTUAL_SERVICE_NAME, buildVirtualServiceName(api.getService(), api.getName(), gw))
                            .put(TemplateConst.VIRTUAL_SERVICE_SUBSET_NAME, subset)
                            .put(TemplateConst.VIRTUAL_SERVICE_ROUTE_YAML, route)
                            .put(TemplateConst.VIRTUAL_SERVICE_EXTRA_YAML, productExtra(vsParams))
                            .put(TemplateConst.SERVICE_INFO_VIRTUAL_SERVICE_SUBSET_NAME, subset);

                    tmpParams.put(TemplateConst.VIRTUAL_SERVICE_META_YAML, produceMeta(tmpParams));
                    return tmpParams;
                })
                .collect(Collectors.toList());

        return collect;
    }

    String buildVirtualServiceName(String serviceName, String apiName, String gw) {
        return String.format("%s-%s-%s", serviceName, apiName, gw);
    }

    String productExtra(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceExtra, params);
    }

    public String produceMatch(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceMatch, params);
    }

    String produceHttpRetry(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceHttpRetry, params);
    }

    String produceMatchPriority(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceMatchPriority, params);
    }

    String produceMeta(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceMeta, params);
    }

    String buildVirtualServiceSubsetName(String serviceName, String apiName, String gw) {
        return String.format("%s-%s-%s", serviceName, apiName, gw);
    }

    String buildGatewayName(String service, String gw) {
        return gw;
    }

    String produceRoute(API api, List<Endpoint> endpoints, String subset) {

        if (simple) return "";
        List<Map<String, Object>> destinations = new ArrayList<>();
        List<String> proxies = api.getProxyUris();

        for (int i = 0; i < proxies.size(); i++) {
            boolean isMatch = false;
            for (Endpoint e : endpoints) {
                if (e.getHostname().equals(proxies.get(i))) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("port", e.getPort());
                    int weight = 100 / proxies.size();
                    if (i == proxies.size() - 1) {
                        weight = 100 - 100 * (proxies.size() - 1) / proxies.size();
                    }
                    param.put("weight", weight);
                    param.put("host", e.getHostname());
                    param.put("subset", subset);
                    destinations.add(param);
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch)
                throw new ApiPlaneException(String.format("%s:%s", ExceptionConst.TARGET_SERVICE_NON_EXIST, proxies.get(i)));
        }

        String destinationStr = subModelProcessor.process(apiVirtualServiceRoute, TemplateParams.instance().put(
            TemplateConst.VIRTUAL_SERVICE_DESTINATIONS, destinations));
        return destinationStr;
    }

    String decorateHost(String code) {
        return String.format("com.netease.%s", code);
    }

    int calculatePluginPriority(API api, Object parentPriority) {
        if (Objects.nonNull(parentPriority) && parentPriority instanceof Integer) {
            return (Integer) parentPriority + 1;
        } else {
            return PriorityUtil.calculate(api);
        }
    }
}
