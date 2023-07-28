package org.hango.cloud.core.plugin.processor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 静态降级插件
 * example:
 * {"requestHost":{"match_type":"exact_match","value":"abc.com"},"requestHeaders":[{"requestHeaderKey":"aaa","_formTableKey":1685342272493,"match_type":"exact_match","requestHeaderValue":"bbb"}],"responseHeaders":[{"responseHeaderKey":"ccc","responseHeaderValue":"ddd","_formTableKey":1685342278064,"match_type":"exact_match"}],"downgradeResponseHeaders":[],"kind":"static-downgrade","requestMethod":{"requestMethodList":["GET","POST"]},"downgradeResponseBody":"qa test rsp","downgradeResponseCode":"200","requestSwitch":true,"requestPath":{"match_type":"exact_match","value":"/get"},"responseCode":{"match_type":"exact_match","value":"500"}}
 *
 **/
@Component
public class StaticDowngradeProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static String RQX_HEADERS = "$.downgrade_rqx.headers";
    private static String SAFE_REGEX_MATCH = "safe_regex_match";
    private static String DOWNGRADE_RPX_HEADERS = "$.downgrade_rpx.headers";
    @Override
    public String getName() {
        return "StaticDowngradeProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{\"downgrade_rpx\":{\"headers\":[]},\"static_response\":{\"http_status\":0}}");
        buildRequestPluginGenerator(source,builder);
        buildResponsePluginGenerator(source,builder);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setGatewayPluginsFragment(wrapper);
        return fragmentHolder;
    }
    private PluginGenerator buildRequestPluginGenerator(PluginGenerator source,PluginGenerator builder){
        buildRequestHeaderPluginGenerator(source,builder);
        buildRequestHostPluginGenerator(source,builder);
        if (source.contain("$.requestMethod")) {
            List<String> method = source.getValue("$.requestMethod.requestMethodList", List.class);
            if (nonNull(method)) {
                if (method.size() == 1) {
                    builder.addJsonElement(RQX_HEADERS, String.format(exact_string_match, ":method", method.get(0)));
                } else if (method.size() > 1) {
                    builder.addJsonElement(RQX_HEADERS, String.format(safe_regex_string_match, ":method", String.join("|", method)));
                }
            }
        }
        if (source.contain("$.requestPath")) {
            String matchType = source.getValue("$.requestPath.match_type", String.class);
            String path = source.getValue("$.requestPath.value", String.class);
            if (nonNull(matchType, path)) {
                if (SAFE_REGEX_MATCH.equals(matchType)) {
                    builder.addJsonElement(RQX_HEADERS, String.format(safe_regex_string_match, ":path", path));
                } else {
                    builder.addJsonElement(RQX_HEADERS, String.format(exact_string_match, ":path", path));
                }
            }
        }
        return builder;
    }
    private PluginGenerator buildRequestHeaderPluginGenerator(PluginGenerator source,PluginGenerator builder){
        builder.createOrUpdateJson("$", "downgrade_rqx", "{\"headers\":[]}");
        if (source.contain("$.requestHeaders")) {
            List<Map<String, String>> headers = source.getValue("$.requestHeaders", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("requestHeaderKey");
                String headerValue = item.get("requestHeaderValue");
                if (haveNull(matchType, headerKey, headerValue)) return;
                if (SAFE_REGEX_MATCH.equals(matchType)) {
                    builder.addJsonElement(RQX_HEADERS, String.format(safe_regex_string_match, headerKey, headerValue));
                } else {
                    builder.addJsonElement(RQX_HEADERS, String.format(exact_string_match, headerKey, headerValue));
                }
            });
        }
        return builder;
    }
    private PluginGenerator buildRequestHostPluginGenerator(PluginGenerator source,PluginGenerator builder){
        if (source.contain("$.requestHost")) {
            String matchType = source.getValue("$.requestHost.match_type", String.class);
            String host = source.getValue("$.requestHost.value", String.class);
            if (nonNull(matchType, host)) {
                if (SAFE_REGEX_MATCH.equals(matchType)) {
                    builder.addJsonElement(RQX_HEADERS, String.format(safe_regex_string_match, ":authority", host));
                } else {
                    builder.addJsonElement(RQX_HEADERS, String.format(exact_string_match, ":authority", host));
                }
            }
        }
        return builder;
    }
    private PluginGenerator buildResponsePluginGenerator(PluginGenerator source,PluginGenerator builder){
        if (source.contain("$.responseHeaders")) {
            List<Map<String, String>> headers = source.getValue("$.responseHeaders", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("responseHeaderKey");
                String headerValue = item.get("responseHeaderValue");
                if (haveNull(matchType, headerKey, headerValue)) return;
                if (SAFE_REGEX_MATCH.equals(matchType)) {
                    builder.addJsonElement(DOWNGRADE_RPX_HEADERS, String.format(safe_regex_string_match, headerKey, headerValue));
                } else {
                    builder.addJsonElement(DOWNGRADE_RPX_HEADERS, String.format(exact_string_match, headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.responseCode")) {
            String matchType = source.getValue("$.responseCode.match_type", String.class);
            String code = source.getValue("$.responseCode.value", String.class);
            if (nonNull(matchType,code)) {
                builder.addJsonElement(DOWNGRADE_RPX_HEADERS, String.format(safe_regex_string_match, ":status", code+"|"));
            }
        }
        builder.updateValue("$.static_response.http_status", source.getValue("$.downgradeResponseCode", Integer.class));
        if (source.contain("$.downgradeResponseHeaders")) {
            builder.createOrUpdateJson("$.static_response", "headers", "[]");
            List<Map<String,String>> headers = source.getValue("$.downgradeResponseHeaders", List.class);
            for (Map<String, String> entry : headers) {
                builder.addJsonElement("$.static_response.headers", String.format("{\"key\":\"%s\",\"value\":\"%s\"}", entry.get("downgradeResponseHeaderKey"), entry.get("downgradeResponseHeaderValue")));
            }
        }
        if (source.contain("$.downgradeResponseBody")) {
            String body = StringEscapeUtils.escapeJava(source.getValue("$.downgradeResponseBody", String.class));
            builder.createOrUpdateJson("$.static_response", "body", String.format("{\"inline_string\": \"%s\"}", body));
        }
        return builder;
    }
}
