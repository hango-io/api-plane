package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/8
 **/
@Component
public class CircuitBreakerProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "CircuitBreakerProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}");
        buildResponse(source, builder);
        buildConfig(source, builder);
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

    private void buildResponse(PluginGenerator source, PluginGenerator builder) {
        if (!source.contain("$.response")) return;
        builder.createOrUpdateJson("$", "response", "{}");
        if (source.contain("$.response.code")) {
            String code = source.getValue("$.response.code");
            builder.createOrUpdateValue("$.response", "http_status", Integer.parseInt(code));
        }
        if (source.contain("$.response.body")) {
            String body = source.getValue("$.response.body");
            if (StringUtils.isNotBlank(body)) {
                builder.createOrUpdateJson("$.response", "body", String.format("{\"inline_string\":\"%s\"}", StringEscapeUtils.escapeJava(body)));
            }
        }
        if (source.contain("$.response.headers")) {
            builder.createOrUpdateJson("$.response", "headers", "[]");
            int length = source.getValue("$.response.headers.length()");
            for (int i = 0; i < length; i++) {
                String key = source.getValue(String.format("$.response.headers[%s].key", i));
                String value = source.getValue(String.format("$.response.headers[%s].value", i));
                builder.addJsonElement("$.response.headers", String.format("{\"key\":\"%s\",\"value\":\"%s\"}", key, value));
            }
        }
    }

    private void buildConfig(PluginGenerator source, PluginGenerator builder) {
        if (!source.contain("$.config")) return;
        if (source.contain("$.config.consecutive_slow_requests")) {
            Long consecutive_slow_requests = Long.valueOf(source.getValue("$.config.consecutive_slow_requests"));
            builder.createOrUpdateValue("$", "consecutive_slow_requests", consecutive_slow_requests);
        }
        if (source.contain("$.config.average_response_time")) {
            Double average_response_time = Double.valueOf(source.getValue("$.config.average_response_time"));
            builder.createOrUpdateValue("$", "average_response_time", average_response_time + "s");
        }
        if (source.contain("$.config.min_request_amount")) {
            Long min_request_amount = Long.valueOf(source.getValue("$.config.min_request_amount"));
            builder.createOrUpdateValue("$", "min_request_amount", min_request_amount);
        }
        if (source.contain("$.config.error_percent_threshold")) {
            Double error_percent_threshold = Double.valueOf(source.getValue("$.config.error_percent_threshold"));
            builder.createOrUpdateJson("$", "error_percent_threshold", String.format("{\"value\":%s}", error_percent_threshold));
        }
        if (source.contain("$.config.break_duration")) {
            Double break_duration = Double.valueOf(source.getValue("$.config.break_duration"));
            builder.createOrUpdateValue("$", "break_duration", break_duration + "s");
        }
        if (source.contain("$.config.lookback_duration")) {
            Double lookback_duration = Double.valueOf(source.getValue("$.config.lookback_duration"));
            builder.createOrUpdateValue("$", "lookback_duration", lookback_duration + "s");
        }
        builder.createOrUpdateValue("$", "wait_body",  "true");
    }

}
