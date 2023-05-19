package org.hango.cloud.util;

import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.meta.GatewayPlugin;
import org.hango.cloud.util.constant.PluginConstant;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


public class HandlerUtil {

    /**
     * 分配插件
     *
     * @param fragments
     * @param matchPlugins
     * @param apiPlugins
     * @param hostPlugins
     */
    public static void distributePlugins(List<FragmentWrapper> fragments,
                                         List<String> matchPlugins,
                                         Map<String, List<String>> apiPlugins,
                                         List<String> hostPlugins) {
        fragments.stream()
                .forEach(f -> {
                    switch (f.getFragmentType()) {
                        case VS_MATCH:
                            matchPlugins.add(f.getContent());
                            break;
                        case VS_API:
                            String userId = StringUtils.isEmpty(f.getXUserId()) ?
                                    PluginConstant.DEFAULT_USER_ID : f.getXUserId();
                            apiPlugins.computeIfAbsent(userId, k -> new ArrayList<>()).add(f.getContent());
                            break;
                        case VS_HOST:
                            hostPlugins.add(f.getContent());
                            break;
                        default:
                    }
                });
    }

    /**
     * 获取插件Map
     *
     * @param fragments CRD片段
     * @return 以用户ID为key，插件CRD文本集合为value的Map对象
     */
    public static Map<String, List<String>> getGatewayPlugins(List<FragmentWrapper> fragments) {
        if (CollectionUtils.isEmpty(fragments)) {
            return Collections.EMPTY_MAP;
        }

        Map<String, List<String>> pluginMap = new HashMap<>();
        fragments.stream()
                .filter(f -> f.getFragmentType().equals(FragmentTypeEnum.VS_API))
                .forEach(f -> {
                    String userId =
                            StringUtils.isEmpty(f.getXUserId()) ? PluginConstant.DEFAULT_USER_ID : f.getXUserId();
                    pluginMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(f.getContent());
                });
        return pluginMap;
    }

    /**
     * 通过CR片段片段获取SmartLimiter资源
     *
     * @param fragments CR片段
     * @return SmartLimiter插件集合
     */
    public static List<String> getSmartLimiters(List<FragmentWrapper> fragments) {
        if (CollectionUtils.isEmpty(fragments)) {
            return Collections.EMPTY_LIST;
        }

        return fragments.stream()
                .filter(f -> f.getFragmentType().equals(FragmentTypeEnum.SMART_LIMIT))
                .map(FragmentWrapper::getContent)
                .collect(Collectors.toList());
    }

    /**
     * 获取网关插件CR中的路由集合，案例如下
     * [127.0.0.1/1000, 127.0.0.1/1001, 127.0.0.1/1002]
     *
     * @param plugin 网关插件
     * @return 路由集合
     */
    public static String getRoute(GatewayPlugin plugin) {
        return "/" + plugin.getRouteId();
    }

    /**
     * 获取网关插件CR名称，例如: "99-prod-gateway"
     *
     * @param plugin 网关插件
     * @return 网关插件CR名称
     */
    public static String getGatewayPluginName(GatewayPlugin plugin) {
        String pluginName = PluginConstant.DEFAULT_PLUGIN_NAME;

        if (plugin.isRoutePlugin()) {
            pluginName = plugin.getRouteId();
        } else if (plugin.isGlobalPlugin()) {
            pluginName = plugin.getCode().toLowerCase();
        }
        return pluginName;
    }

    /**
     * 获取带端口的hosts列表
     * [127.0.0.1, 127.0.0.1] -> [127.0.0.1:80, 127.0.0.1:80]
     *
     * @param plugin 网关插件
     * @return hosts列表
     */
    public static List<String> completeHosts(GatewayPlugin plugin) {
        List<String> hosts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(plugin.getHosts())){
            Integer port = plugin.getPort();
            hosts = plugin.getHosts().stream().map(host -> host + ":" + port).collect(Collectors.toList());
        }
        return hosts;
    }

    /**
     * 构建VS名称
     *
     * @param apiName 路由名称
     * @param projectId 项目ID
     * @param gw 网关标签
     * @return VS名称
     */
    public static String buildVirtualServiceName(String apiName, String projectId, String gw) {
        return String.format("%s-%s-%s", apiName, projectId, gw);
    }
}

