package org.hango.cloud.core.plugin.processor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * 熔断插件
 * example:
 {"rt":{"consecutiveSlowRequests":"2","rtThreshold":"1"},"breakType":["RTCircuitbreaker","ErrorPercentCircuitbreaker"],"response":{"headers":[{"_formTableKey":1685340549421,"value":"ccc","key":"aaa"},{"_formTableKey":1685340549421,"value":"eee","key":"ddd"}],"code":"456","body":"aasdadasdagsDBSDFDSAD"},"kind":"circuit-breaker","errorPercent":{"minRequestAmount":"2","errorPercentThreshold":"1"},"breakDuration":"10","lookbackDuration":"6"}
 */
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
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setGatewayPluginsFragment(wrapper);
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
        if (!source.contain("$.breakType")) return;
        if (source.contain("$.rt.consecutiveSlowRequests")) {
            Long consecutive_slow_requests = Long.valueOf(source.getValue("$.rt.consecutiveSlowRequests"));
            builder.createOrUpdateValue("$", "consecutive_slow_requests", consecutive_slow_requests);
        }
        if (source.contain("$.rt.rtThreshold")) {
            Double average_response_time = Double.valueOf(source.getValue("$.rt.rtThreshold"));
            builder.createOrUpdateValue("$", "average_response_time", average_response_time + "s");
        }
        if (source.contain("$.errorPercent.minRequestAmount")) {
            Long min_request_amount = Long.valueOf(source.getValue("$.errorPercent.minRequestAmount"));
            builder.createOrUpdateValue("$", "min_request_amount", min_request_amount);
        }
        if (source.contain("$.errorPercent.errorPercentThreshold")) {
            Double error_percent_threshold = Double.valueOf(source.getValue("$.errorPercent.errorPercentThreshold"));
            builder.createOrUpdateJson("$", "error_percent_threshold", String.format("{\"value\":%s}", error_percent_threshold));
        }
        if (source.contain("$.breakDuration")) {
            Double break_duration = Double.valueOf(source.getValue("$.breakDuration"));
            builder.createOrUpdateValue("$", "break_duration", break_duration + "s");
        }
        if (source.contain("$.lookbackDuration")) {
            Double lookback_duration = Double.valueOf(source.getValue("$.lookbackDuration"));
            builder.createOrUpdateValue("$", "lookback_duration", lookback_duration + "s");
        }
        builder.createOrUpdateValue("$", "wait_body",  "true");
    }

}
