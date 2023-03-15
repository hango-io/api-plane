package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DynamicDowngradeProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "DynamicDowngradeProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{\"downgrade_rpx\":{\"headers\":[]},\"cache_rpx_rpx\":{\"headers\":[]},\"cache_ttls\":{\"RedisHttpCache\":{},\"LocalHttpCache\":{}},\"key_maker\":{\"query_params\":[],\"headers_keys\":[]}}");
        if (source.contain("$.cache")) {
            createCondition(source, builder);
            createCacheRpx(source, builder);
            createCacheTtls(source, builder);
            createKeyMaker(source, builder);
        }
        if (source.contain("$.httpx") || plugin.contains("httpx")) {
            if (source.contain("$.httpx.uri") || source.contain("$.httpx.remote")) {
                builder = PluginGenerator.newInstance("{\"downgrade_rpx\":{\"headers\":[]}}");
                createCondition(source, builder);
                createHttpx(source, builder);
            }
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

    private void createCondition(PluginGenerator source, PluginGenerator builder) {
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
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex_string_match, headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact_string_match, headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.request.host")) {
            String matchType = source.getValue("$.condition.request.host.match_type", String.class);
            String host = source.getValue("$.condition.request.host.value", String.class);
            if (nonNull(matchType, host)) {
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex_string_match, ":authority", host));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact_string_match, ":authority", host));
                }
            }
        }
        if (source.contain("$.condition.request.method")) {
            List<String> method = source.getValue("$.condition.request.method", List.class);
            if (method.size() == 1) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact_string_match, ":method", method.get(0)));
            } else if (method.size() > 1) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex_string_match, ":method", String.join("|", method)));
            }
        }
        if (source.contain("$.condition.request.path")) {
            String matchType = source.getValue("$.condition.request.path.match_type", String.class);
            String path = source.getValue("$.condition.request.path.value", String.class);
            if (nonNull(matchType, path)) {
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(safe_regex_string_match, ":path", path));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format(exact_string_match, ":path", path));
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
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format(safe_regex_string_match, headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format(exact_string_match, headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.response.code")) {
            String matchType = source.getValue("$.condition.response.code.match_type", String.class);
            String code = source.getValue("$.condition.response.code.value", String.class);
            if (nonNull(code)) {
                builder.addJsonElement("$.downgrade_rpx.headers", String.format(safe_regex_string_match, ":status", code+"|"));
            }
        }
    }

    private void createCacheRpx(PluginGenerator source, PluginGenerator builder) {
        builder.createOrUpdateJson("$", "cache_rpx_rpx", "{\"headers\":[]}");
        if (source.contain("$.cache.condition.response.code")) {
            String mathchType = source.getValue("$.cache.condition.response.code.match_type");
            String code = source.getValue("$.cache.condition.response.code.value");
            if (nonNull(code)) {
                builder.addJsonElement("$.cache_rpx_rpx.headers", String.format(safe_regex_string_match, ":status", code+"|"));
            }
        }
        if (source.contain("$.cache.condition.response.headers")) {
            List<Map<String, String>> headers = source.getValue("$.cache.condition.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey, headerValue)) return;
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.cache_rpx_rpx.headers", String.format(safe_regex_string_match, headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.cache_rpx_rpx.headers", String.format(exact_string_match, headerKey, headerValue));
                }
            });
        }
    }

    private void createCacheTtls(PluginGenerator source, PluginGenerator builder) {
        Integer redisDefaultTtl = source.getValue("$.cache.ttl.default", Integer.class);
        //redis ttl
        if (source.contain("$.cache.ttl.default")) {
            builder.createOrUpdateValue("$.cache_ttls.RedisHttpCache", "default", redisDefaultTtl * 1000);
            builder.createOrUpdateValue("$.cache_ttls.LocalHttpCache", "default", redisDefaultTtl * 1000);
        }
        if (source.contain("$.cache.ttl.custom")) {
            List<Map<String, Object>> customs = source.getValue("$.cache.ttl.custom", List.class);
            builder.createOrUpdateJson("$.cache_ttls.RedisHttpCache", "customs", "{}");
            builder.createOrUpdateJson("$.cache_ttls.LocalHttpCache", "customs", "{}");
            customs.forEach(item -> {
                String code = (String) Optional.ofNullable(item.get("code")).orElse("200");
                Object ttl = item.get("ttl");
                builder.createOrUpdateValue("$.cache_ttls.RedisHttpCache.customs", code, ttl);
                builder.createOrUpdateValue("$.cache_ttls.LocalHttpCache.customs", code, ttl);
            });
        }
    }

    private void createKeyMaker(PluginGenerator source, PluginGenerator builder) {
        Boolean ignoreCase = source.getValue("$.cache.cache_key.ignoreCase", Boolean.class);
        if (nonNull(ignoreCase)) {
            builder.updateValue("$.key_maker.ignore_case", ignoreCase);
        }
        if (source.contain("$.cache.cache_key.query_params")) {
            List<String> queryParams = source.getValue("$.cache.cache_key.query_params", List.class);
            queryParams.forEach(item -> {
                builder.addElement("$.key_maker.query_params", item);
            });
        }
        if (source.contain("$.cache.cache_key.headers")) {
            List<String> headers = source.getValue("$.cache.cache_key.headers", List.class);
            headers.forEach(item -> {
                builder.addJsonElement("$.key_maker.headers_keys", item);
            });
        }
    }

    private void createHttpx(PluginGenerator source, PluginGenerator builder) {
        builder.createOrUpdateValue("$", "downgrade_src", "HTTPX");
        if (source.contain("$.httpx.uri")) {
            String uri = source.getValue("$.httpx.uri");
            builder.createOrUpdateValue("$", "downgrade_uri", uri);
        }
        if (source.contain("$.httpx.remote") && source.getValue("$.httpx.remote.requestSwitch", Boolean.class)) {
            builder.createOrUpdateJson("$", "override_remote", "{}");
            // 服务案例: "outbound|80|dynamic-5314-demo-gateway|istio-e2e-app.apigw-demo.svc.cluster.local"
            builder.createOrUpdateValue("$.override_remote", "cluster", source.getValue("$.httpx.remote.cluster"));
            builder.createOrUpdateValue("$.override_remote", "timeout", source.getValue("$.httpx.remote.timeout", Integer.class) + "s");
        }
    }
}
