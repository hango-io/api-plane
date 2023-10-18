package org.hango.cloud.meta.enums;

import java.util.stream.Stream;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/21
 */
@SuppressWarnings({"java:S115","java:S1192"})
public enum PluginProcessorEnum {

    rewrite("rewrite","RewriteProcessor"),
    jsonp("jsonp","JsonpProcessor"),
    transformer("transformer","TransformProcessor"),
    static_downgrade("static-downgrade","StaticDowngradeProcessor"),
    dynamic_downgrade("dynamic-downgrade","DynamicDowngradeProcessor"),
    local_limiting("local-limiting","SmartLimiterProcessor"),
    rate_limiting("rate-limiting","SmartLimiterProcessor"),
    cluster_group_limiting("cluster-group-limiting","SmartLimiterProcessor"),
    ianus_percent_limit("ianus-percent-limit","FlowLimitProcessor"),
    ip_restriction("ip-restriction","IpRestrictionProcessor"),
    ua_restriction("ua-restriction","UaRestrictionProcessor"),
    referer_restriction("referer-restriction","RefererRestrictionProcessor"),
    header_restriction("header-restriction","HeaderRestrictionProcessor"),
    traffic_mark("traffic-mark","TrafficMarkProcessor"),
    response_header_rewrite("response-header-rewrite","ResponseHeaderRewriteProcessor"),
    request_rewrite("request-rewrite","RequestRewriterProcessor"),
    cors("cors","CorsProcessor"),
    cache("cache","Cache"),
    local_cache("local-cache","LocalCache"),
    redis_cache("redis-cache","RedisCache"),
    // 兼容21.0.x版本认证插件，22.0.x版本认证插件已拆分为simple-auth、jwt-auth和oauth2-auth
    super_auth("super-auth","PreviousVersionSuperAuth"),
    oauth2_auth("oauth2-auth","Oauth2Auth"),
    simple_auth("simple-auth","SimpleAuth"),
    jwt_auth("jwt-auth","JwtAuth"),
    basic_rbac("basic-rbac","BasicRbac"),
    circuit_breaker("circuit-breaker","CircuitBreakerProcessor"),
    soap_json_transcoder("soap-json-transcoder","SoapJsonTranscoderProcessor"),
    ianus_router("ianus-router","RouteProcessor"),
    waf("waf","WafProcessor"),
    request_body_rewrite("request-body-rewrite","RequestBodyReWriteProcessor"),
    response_body_rewrite("response-body-rewrite","ResponseBodyReWriteProcessor"),
    parameter_validate("parameters-validate","ParameterValidateProcessor"),
    session_state("session-state","SessionStatePerRouteProcessor"),

    rider("rider","LuaProcessor"),
    wasm("wasm","WasmProcessor"),


            ;

    /**
     * 插件映射名称
     */
    private String pluginName;


    /**
     * 插件处理processor名称
     */
    private String processorName;

    PluginProcessorEnum(String pluginName, String processorName) {
        this.pluginName = pluginName;
        this.processorName = processorName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getProcessorName() {
        return processorName;
    }


    public static PluginProcessorEnum get(String pluginName){
        return Stream.of(values()).filter(pluginMapping -> pluginMapping.getPluginName().equals(pluginName)).findFirst().orElse(null);
    }

}
