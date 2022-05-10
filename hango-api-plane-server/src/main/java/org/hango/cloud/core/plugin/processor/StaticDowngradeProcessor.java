package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 静态降级插件
 *
 **/
@Component
public class StaticDowngradeProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "StaticDowngradeProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{\"downgrade_rpx\":{\"headers\":[]},\"static_response\":{\"http_status\":0}}");
        if (source.contain("$.condition.request")) {
            builder.createOrUpdateJson("$", "downgrade_rqx", "{\"headers\":[]}");
        }
        if (source.contain("$.condition.request.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.request.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey, headerValue)) return;
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex, headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact, headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.request.host")) {
            String matchType = source.getValue("$.condition.request.host.match_type", String.class);
            String host = source.getValue("$.condition.request.host.value", String.class);
            if (nonNull(matchType, host)) {
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex, ":authority", host));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact, ":authority", host));
                }
            }
        }
        if (source.contain("$.condition.request.method")) {
            List<String> method = source.getValue("$.condition.request.method", List.class);
            if (nonNull(method)) {
                if (method.size() == 1) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact, ":method", method.get(0)));
                } else if (method.size() > 1) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex, ":method", String.join("|", method)));
                }
            }
        }
        if (source.contain("$.condition.request.path")) {
            String matchType = source.getValue("$.condition.request.path.match_type", String.class);
            String path = source.getValue("$.condition.request.path.value", String.class);
            if (nonNull(matchType, path)) {
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex, ":path", path));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact, ":path", path));
                }
            }
        }
        if (source.contain("$.condition.response.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey, headerValue)) return;
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format(safe_regex, headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format(exact, headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.response.code")) {
            String matchType = source.getValue("$.condition.response.code.match_type", String.class);
            String code = source.getValue("$.condition.response.code.value", String.class);
            if (nonNull(code)) {
                builder.addJsonElement("$.downgrade_rpx.headers", String.format(safe_regex, ":status", code+"|"));
            }
        }
        builder.updateValue("$.static_response.http_status", source.getValue("$.response.code", Integer.class));
        if (source.contain("$.response.headers")) {
            builder.createOrUpdateJson("$.static_response", "headers", "[]");
            Map<String, String> headers = source.getValue("$.response.headers", Map.class);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addJsonElement("$.static_response.headers", String.format("{\"key\":\"%s\",\"value\":\"%s\"}", entry.getKey(), entry.getValue()));
            }
        }
        if (source.contain("$.response.body")) {
            String body = StringEscapeUtils.escapeJava(source.getValue("$.response.body", String.class));
            builder.createOrUpdateJson("$.static_response", "body", String.format("{\"inline_string\": \"%s\"}", body));
        }
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
