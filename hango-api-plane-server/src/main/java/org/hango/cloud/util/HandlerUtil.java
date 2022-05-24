package org.hango.cloud.util;

import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.util.constant.PluginConstant;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;


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


}

