package org.hango.cloud.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/21
 */
@SuppressWarnings({"java:S115","java:S1192"})
public enum PluginMapping {

    rewrite("rewrite","proxy.filters.http.path_rewrite","","RewriteProcessor"),
    jsonp("jsonp","proxy.filters.http.jsonpfilter","","JsonpProcessor"),
    transformer("transformer","proxy.filters.http.transformation","","TransformProcessor"),
    static_downgrade("static-downgrade","proxy.filters.http.staticdowngrade","","StaticDowngradeProcessor"),
    dynamic_downgrade("dynamic-downgrade","proxy.filters.http.dynamicdowngrade","","DynamicDowngradeProcessor"),
    local_limiting("local-limiting","","","SmartLimiterProcessor"),
    rate_limiting("rate-limiting","","","SmartLimiterProcessor"),
    cluster_group_limiting("cluster-group-limiting","","","SmartLimiterProcessor"),
    ianus_percent_limit("ianus-percent-limit","envoy.filters.http.fault","","FlowLimitProcessor"),
    ip_restriction("ip-restriction","proxy.filters.http.iprestriction","","IpRestrictionProcessor"),
    ua_restriction("ua-restriction","proxy.filters.http.ua_restriction","","UaRestrictionProcessor"),
    referer_restriction("referer-restriction","proxy.filters.http.referer_restriction","","RefererRestrictionProcessor"),
    header_restriction("header-restriction","proxy.filters.http.header_restriction","","HeaderRestrictionProcessor"),
    traffic_mark("traffic-mark","proxy.filters.http.header_rewrite","type.googleapis.com/proxy.filters.http.header_rewrite.v2.ProtoRouteConfig","TrafficMarkProcessor"),
    response_header_rewrite("response-header-rewrite","proxy.filters.http.header_rewrite","type.googleapis.com/proxy.filters.http.header_rewrite.v2.ProtoRouteConfig","ResponseHeaderRewriteProcessor"),
    request_rewrite("request-rewrite","proxy.filters.http.header_rewrite","type.googleapis.com/proxy.filters.http.header_rewrite.v2.ProtoRouteConfig","RequestRewriterProcessor"),
    cors("cors","envoy.filters.http.cors","","CorsProcessor"),
    cache("cache","proxy.filters.http.super_cache","","Cache"),
    local_cache("local-cache","proxy.filters.http.local_cache","","LocalCache"),
    redis_cache("redis-cache","proxy.filters.http.redis_cache","","RedisCache"),
    // 兼容21.0.x版本认证插件，22.0.x版本认证插件已拆分为sign-auth、jwt-auth和oauth2-auth
    super_auth("super-auth","proxy.filters.http.super_authz","","PreviousVersionSuperAuth"),
    oauth2_auth("oauth2-auth","proxy.filters.http.super_authz","","Oauth2Auth"),
    simple_auth("simple-auth","proxy.filters.http.super_authz","","SimpleAuth"),
    jwt_auth("jwt-auth","envoy.filters.http.jwt_authn","","JwtAuth"),
    basic_rbac("basic-rbac","envoy.filters.http.rbac","","BasicRbac"),
    circuit_breaker("circuit-breaker","proxy.filters.http.circuitbreaker","","CircuitBreakerProcessor"),
    soap_json_transcoder("soap-json-transcoder","proxy.filters.http.soapjsontranscoder","","SoapJsonTranscoderProcessor"),
    ianus_router("ianus-router","envoy.filters.http.fault","","RouteProcessor"),
    waf("waf","proxy.filters.http.waf","","WafProcessor"),
    request_body_rewrite("request-body-rewrite","proxy.filters.http.request_body_transformation","","RequestBodyReWriteProcessor"),
    response_body_rewrite("response-body-rewrite","proxy.filters.http.response_body_transformation","","ResponseBodyReWriteProcessor"),
    parameter_validate("parameters-validate","proxy.filters.http.parameter_validate","","ParameterValidateProcessor"),


    lua("lua","proxy.filters.http.rider","type.googleapis.com/proxy.filters.http.rider.v3alpha1.RouteFilterConfig","LuaProcessor"),
    wasm("wasm","proxy.filters.http.wasm","type.googleapis.com/envoy.extensions.filters.http.wasm.v3.RoutePluginConfig","WasmProcessor"),
    session_state("session-state","envoy.filters.http.stateful_session","type.googleapis.com/envoy.extensions.filters.http.stateful_session.v3.StatefulSessionPerRoute","SessionStatePerRouteProcessor"),



            ;

    /**
     * 插件映射名称
     */
    private String mappingName;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件类型url
     */
    private String typeUrl;

    /**
     * 插件处理processor名称
     */
    private String processorName;

    PluginMapping(String mappingName, String name, String typeUrl, String processorName) {
        this.mappingName = mappingName;
        this.name = name;
        this.typeUrl = typeUrl;
        this.processorName = processorName;
    }

    public String getMappingName() {
        return mappingName;
    }

    public String getName() {
        return name;
    }

    public String getProcessorName() {
        return processorName;
    }

    public String getTypeUrl() {
        return typeUrl;
    }

    public static PluginMapping getBymappingName(String mappingName){
        if (StringUtils.isBlank(mappingName)){
            return null;
        }
        for (PluginMapping value : values()) {
            if (value.getMappingName().equals(mappingName)) {
                return value;
            }
        }
        return null;
    }

    public static String getName(String kind){
        for (PluginMapping value : values()) {
            if (value.getMappingName().equals(kind)) {
                return value.getName();
            }
        }
        return kind;
    }

}
