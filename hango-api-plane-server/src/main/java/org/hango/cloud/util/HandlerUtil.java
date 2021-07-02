package org.hango.cloud.util;

import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class HandlerUtil {


    public static List<String> extractFragments(List<FragmentWrapper> fragments) {
        List<String> plugins = Collections.emptyList();
        if (!CollectionUtils.isEmpty(fragments)) {
            plugins = fragments.stream()
                    .filter(f -> f != null)
                    .map(f -> f.getContent())
                    .collect(Collectors.toList());
        }
        return plugins;
    }

    public static final String DEFAULT_USER_ID = "";
    /**
     * 分配插件
     *
     * @param fragments
     * @param matchPlugins
     * @param apiPlugins
     * @param hostPlugins
     */
    public static void distributePlugins(List<FragmentWrapper> fragments, List<String> matchPlugins, Map<String, List<String>> apiPlugins, List<String> hostPlugins) {
        fragments.stream()
                .forEach(f -> {
                    switch (f.getFragmentType()) {
                        case VS_MATCH:
                            matchPlugins.add(f.getContent());
                            break;
                        case VS_API:
                            String userId = StringUtils.isEmpty(f.getXUserId()) ? DEFAULT_USER_ID : f.getXUserId();
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
     * 获取api级别的插件
     * @param fragments
     * @return
     */
    public static Map<String, List<String>> getApiPlugins(List<FragmentWrapper> fragments) {
        if (CollectionUtils.isEmpty(fragments)) return Collections.EMPTY_MAP;

        Map<String, List<String>> apiPlugins = new HashMap<>();
        fragments.stream()
            .filter(f -> f.getFragmentType().equals(FragmentTypeEnum.VS_API))
            .forEach(f -> {
                String userId = StringUtils.isEmpty(f.getXUserId()) ? DEFAULT_USER_ID : f.getXUserId();
                apiPlugins.computeIfAbsent(userId, k -> new ArrayList<>()).add(f.getContent());
            });
        return apiPlugins;
    }


}

