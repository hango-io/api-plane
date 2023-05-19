package org.hango.cloud.util;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.UriMatch;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PriorityUtil {

    //TODO 适配轻舟网关API

    private static final Map<UriMatch, Integer> matchScore =
            ImmutableMap.of(
                UriMatch.exact, 2,
                UriMatch.prefix, 1,
                UriMatch.regex, 0
            );

    public static int calculate(API api) {
        return calculate(api, Collections.emptyList());
    }

    /**
     *
     * @param api
     * @param paths override url
     * @return
     */
    public static int calculate(API api, List<String> paths) {

        List<String> urls = CollectionUtils.isEmpty(paths) ? api.getRequestUris() : paths;
        int urlLen = urls.stream().collect(Collectors.summingInt(url -> url.length()));
        int routeNum = routeNum(api);

        int priority = matchScore.get(api.getUriMatch())*20000 + urlLen*20 + routeNum;
        return priority;
    }

    private static int routeNum(API api) {
        int num = 0;
        if (!CollectionUtils.isEmpty(api.getMethods())) num++;
        if (!CollectionUtils.isEmpty(api.getHosts())) num++;
        if (!CollectionUtils.isEmpty(api.getHeaders())) num += api.getHeaders().size();
        if (!CollectionUtils.isEmpty(api.getQueryParams())) num += api.getQueryParams().size();
        return num;
    }
}
