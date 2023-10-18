package org.hango.cloud.core.plugin.processor;

import io.micrometer.core.instrument.util.StringEscapeUtils;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class LocalLimitProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "LocalLimitProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        PluginGenerator builder = PluginGenerator.newInstance("{\"use_thread_local_token_bucket\":{},\"rate_limit\":[]}");
        List<Object> limits = source.getValue("$.limit_by_list");

        limits.forEach(limit -> {
            PluginGenerator rg = PluginGenerator.newInstance(limit, ResourceType.OBJECT, editorContext);
            getUnits(rg).forEach((unit, duration) -> {
                builder.addJsonElement("$.rate_limit", createRateLimits(rg, unit, duration));
            });
        });
        //使用ThreadLocal，每一个线程单独计数，不进行加锁，提升效率
        if (source.contain("$.IsSafe")  && source.getValue("$.IsSafe", Boolean.class)) {
            builder.createOrUpdateValue("$", "use_thread_local_token_bucket", source.getValue("$.IsSafe"));
        }

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setGatewayPluginsFragment(wrapper);
        return fragmentHolder;
    }

    private String createRateLimits(PluginGenerator rq, String unit, Long duration){
        PluginGenerator builder = PluginGenerator.newInstance("{\"config\":{}}");
        if (rq.contain("$.headers")) {
            builder.createOrUpdateJson("$", "matcher", "{\"headers\":[]}");
//            builder.addJsonElement("$.matcher", String.format("{\"headers\":[]}"));
            List<Map<String, String>> headers = rq.getValue("$.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey, headerValue)) return;
                String jsonValue = StringEscapeUtils.escapeJson(headerValue);
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.matcher.headers", String.format(safe_regex_string_match, headerKey, jsonValue));
                } else {
                    builder.addJsonElement("$.matcher.headers", String.format(exact_string_match, headerKey, jsonValue));
                }
            });
        }
        builder.createOrUpdateValue("$.config", "unit", unit);
        builder.createOrUpdateValue("$.config", "rate", duration);
        return builder.jsonString();
    }

    private Map<String, Long> getUnits(PluginGenerator rg) {
        Map<String, Long> ret = new LinkedHashMap<>();
        String[][] map = new String[][]{
                {"$.second", "SS"},
                {"$.minute", "MM"},
                {"$.hour", "HH"},
                {"$.day", "DD"}
        };
        for (String[] obj : map) {
            Long duration = rg.getValue(obj[0], Long.class);
            if (rg.contain(obj[0]) && Objects.nonNull(duration)) {
                ret.put(obj[1], duration);
            }
        }
        return ret;
    }
}
