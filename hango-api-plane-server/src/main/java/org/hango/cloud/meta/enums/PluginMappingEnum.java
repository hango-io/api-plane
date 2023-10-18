package org.hango.cloud.meta.enums;

import com.google.common.collect.Ordering;
import org.hango.cloud.meta.dto.PluginOrderItemDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hango.cloud.meta.enums.PluginCategoryEnum.POST_BUILTIN;

/**
 * @Author zhufengwei
 * @Date 2023/9/25
 */
public enum PluginMappingEnum {
    //系统前置插件
    METADATAHUB("proxy.filters.http.metadatahub", "", Collections.EMPTY_LIST, PluginCategoryEnum.PRE_BUILTIN, 1),
    //安全类插件
    IP_RESTRICTION("proxy.filters.http.iprestriction", "", Collections.singletonList("ip-restriction"), PluginCategoryEnum.SECURITY, 1),
    UA_RESTRICTION("proxy.filters.http.ua_restriction", "", Collections.singletonList("ua-restriction"), PluginCategoryEnum.SECURITY, 2),
    REFERER_RESTRICTION("proxy.filters.http.referer_restriction", "", Collections.singletonList("referer-restriction"), PluginCategoryEnum.SECURITY, 3),
    HEADER_RESTRICTION("proxy.filters.http.header_restriction", "", Collections.singletonList("header-restriction"), PluginCategoryEnum.SECURITY, 4),
    WAF("proxy.filters.http.waf", "", Collections.singletonList("waf"), PluginCategoryEnum.SECURITY, 5),
    PARAMETER_VALIDATE( "proxy.filters.http.parameter_validate", "", Collections.singletonList("parameters-validate"), PluginCategoryEnum.SECURITY, 6),
    CORS( "envoy.filters.http.cors", "", Collections.singletonList("cors"), PluginCategoryEnum.SECURITY, 7),
    JSONP("proxy.filters.http.jsonpfilter", "", Collections.singletonList("jsonp"), PluginCategoryEnum.SECURITY, 8),


    //认证类插件
    SUPER_AUTHZ("proxy.filters.http.super_authz", "", Arrays.asList("super-authz","oauth2-auth", "simple-auth"), PluginCategoryEnum.AUTH, 1),
    JWT_AUTH("envoy.filters.http.jwt_authn", "", Collections.singletonList("jwt-auth"), PluginCategoryEnum.AUTH, 2),
    BASIC_RBAC("envoy.filters.http.rbac", "", Collections.singletonList("basic-rbac"), PluginCategoryEnum.AUTH, 3),

    //流量管理类插件
    LOCAL_CACHE("proxy.filters.http.local_cache", "", Collections.singletonList("local-cache"), PluginCategoryEnum.TRAFFIC_POLICY, 1),
    REDIS_CACHE("proxy.filters.http.redis_cache", "", Collections.singletonList("redis-cache"), PluginCategoryEnum.TRAFFIC_POLICY, 2),
    SUPER_CACHE("proxy.filters.http.super_cache", "", Collections.singletonList("cache"), PluginCategoryEnum.TRAFFIC_POLICY, 3),
    RATE_LIMITING("envoy.filters.http.ratelimit", "", Arrays.asList("rate-limiting","cluster-group-limiting"), PluginCategoryEnum.TRAFFIC_POLICY, 4),
    LOCAL_LIMITING("envoy.filters.http.local_ratelimit", "", Collections.singletonList("local-limiting"), PluginCategoryEnum.TRAFFIC_POLICY, 5),
    FAULT("envoy.filters.http.fault", "", Collections.singletonList("ianus-percent-limit"), PluginCategoryEnum.TRAFFIC_POLICY, 6),
    STATIC_DOWNGRADE("proxy.filters.http.staticdowngrade", "", Collections.singletonList("static-downgrade"), PluginCategoryEnum.TRAFFIC_POLICY, 7),
    DYNAMIC_DOWNGRADE("proxy.filters.http.dynamicdowngrade", "", Collections.singletonList("dynamic-downgrade"), PluginCategoryEnum.TRAFFIC_POLICY, 8),
    CIRCUIT_BREAKER("proxy.filters.http.circuitbreaker", "", Collections.singletonList("circuit-breaker"), PluginCategoryEnum.TRAFFIC_POLICY, 9),
    
    //数据转换类插件
    PATH_REWRITE("proxy.filters.http.path_rewrite", "", Collections.singletonList("rewrite"), PluginCategoryEnum.DATA_FORMAT, 1),
    HEADER_REWRITE( "proxy.filters.http.header_rewrite", "type.googleapis.com/proxy.filters.http.header_rewrite.v2.ProtoRouteConfig", Arrays.asList("traffic-mark", "response-header-rewrite", "request-rewrite"), PluginCategoryEnum.DATA_FORMAT, 2),
    REQUEST_BODY_REWRITE("proxy.filters.http.request_body_transformation", "", Collections.singletonList("request-body-rewrite"), PluginCategoryEnum.DATA_FORMAT, 3),
    RESPONSE_BODY_REWRITE("proxy.filters.http.response_body_transformation", "", Collections.singletonList("response-body-rewrite"), PluginCategoryEnum.DATA_FORMAT, 4),
    TRANSFORMER("proxy.filters.http.transformation", "", Collections.singletonList("transformer"), PluginCategoryEnum.DATA_FORMAT, 5),
    MSHA_PLUGIN("msha-plugin", "type.googleapis.com/proxy.filters.http.rider.v3alpha1.RouteFilterConfig", Collections.singletonList("msha-plugin"), PluginCategoryEnum.DATA_FORMAT, 6 ),
    //系统后置插件
    TRAFFIC_MARK("proxy.filters.http.traffic_mark", "", Collections.EMPTY_LIST, POST_BUILTIN, 1),
    DETAILED_STATS("proxy.filters.http.detailed_stats", "", Collections.EMPTY_LIST, POST_BUILTIN, 2),
    STATEFUL_SESSION("envoy.filters.http.stateful_session", "type.googleapis.com/envoy.extensions.filters.http.stateful_session.v3.StatefulSessionPerRoute", Collections.EMPTY_LIST, POST_BUILTIN, 3),
    SOAPJSONTRANSCODER("proxy.filters.http.soapjsontranscoder", "", Collections.singletonList("soap-json-transcoder"), POST_BUILTIN, 4),

    //自定义插件
    RIDER("rider", "type.googleapis.com/proxy.filters.http.rider.v3alpha1.RouteFilterConfig", Collections.singletonList("rider"), null, -1 ),
    WASM("wasm", "type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm", Collections.singletonList("wasm"), null, -1 ),
    ;

    //插件映射map,key:displayName,value:internalName
    private static final Map<String, String> pluginMap = new HashMap<>();

    static {
        for (PluginMappingEnum plugin : PluginMappingEnum.values()) {
            for (String pluginName : plugin.getPluginName()) {
                pluginMap.put(pluginName, plugin.getFilterName());
            }
        }
    }

    /**
     * 插件名称例如super-auth,oauth2-auth
     */
    private final List<String> pluginName;

    /**
     * envoy filter名称, 例如proxy.filters.http.super_authz
     */
    private final String filterName;

    /**
     * 插件类型url
     */
    private final String typeUrl;

    /**
     * 插件类型
     */
    private final PluginCategoryEnum category;

    /**
     * 插件执行顺序
     */
    private final Integer order;

    PluginMappingEnum(String filterName, String typeUrl, List<String> pluginName, PluginCategoryEnum category, int order) {
        this.pluginName = pluginName;
        this.typeUrl = typeUrl;
        this.filterName = filterName;
        this.category = category;
        this.order = order;
    }

    public List<String> getPluginName() {
        return pluginName;
    }

    public String getFilterName() {
        return filterName;
    }

    public PluginCategoryEnum getCategory() {
        return category;
    }

    public Integer getOrder() {
        return order;
    }

    public String getTypeUrl() {
        return typeUrl;
    }

    public static PluginMappingEnum get(String filterName) {
        return Stream.of(values()).filter(pluginCategoryEnum -> pluginCategoryEnum.getFilterName().equals(filterName)).findFirst().orElse(null);
    }

    public static PluginMappingEnum getByPluginName(String pluginName) {
        if (!pluginMap.containsKey(pluginName)) {
            return null;
        }
        String filterName = pluginMap.get(pluginName);
        return get(filterName);
    }

    public static String getFilterName(String pluginName) {
        return pluginMap.get(pluginName);
    }

    /**
     * 获取插件的执行顺序
     * 1.获取插件的下一个执行阶段A
     * 2.遍历插件列表,如果插件的执行阶段等于A,则返回当前插件的下标
     * 3.如果没有找到,则返回插件列表的长度
     */
    public static int getPluginIndex(List<String> filterNameList, String pluginCategory) {
        PluginCategoryEnum nextCategory = PluginCategoryEnum.getNextCategory(pluginCategory);
        //如果插件列表没有顺序,则返回最后一个插件的下标
        if (nextCategory == null || !hasOrder(filterNameList)){
            nextCategory = POST_BUILTIN;
        }
        //获取指定执行阶段的第一个插件
        return getFirstIndex(filterNameList, nextCategory);
    }

    public static boolean hasOrder(List<String> filterNameList){
        List<PluginCategoryEnum> pluginCategorys = filterNameList.stream().map(PluginMappingEnum::get).filter(Objects::nonNull).map(PluginMappingEnum::getCategory).collect(Collectors.toList());
        List<Integer> orderList = pluginCategorys.stream().map(PluginCategoryEnum::getOrder).collect(Collectors.toList());
        return Ordering.natural().isOrdered(orderList);
    }


    /**
     * 获取指定插件类型的第一个插件下标
     * ex:pluginCategory= DATA_FORMAT，则获取PATH_REWRITE插件的下标
     */
    public static int getFirstIndex(List<String> internalNameList, PluginCategoryEnum pluginCategory) {
        for (int index = 0; index < internalNameList.size(); index++) {
            String internalName = internalNameList.get(index);
            PluginMappingEnum pluginMappingEnum = PluginMappingEnum.get(internalName);
            if (pluginMappingEnum == null){
                continue;
            }
            if (pluginCategory.equals(pluginMappingEnum.getCategory())) {
                return index;
            }
        }
        return internalNameList.size();
    }

    public static void pluginSort(List<PluginOrderItemDTO> pluginOrderItemDTOS) {
        pluginOrderItemDTOS.sort(Comparator.comparing(item -> {
            int interval = 1000;
            String name = item.getName();
            for (PluginMappingEnum plugin : PluginMappingEnum.values()) {
                if (plugin.getFilterName().equals(name)) {
                    return plugin.getCategory().getOrder() * interval + plugin.getOrder();
                }
            }
            //用户特殊插件放在系统后置插件之前
            return POST_BUILTIN.getOrder() * interval;
        }));
    }
}
