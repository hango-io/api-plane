package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorsProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "CorsProcessor";
    }

    private String allow_origin_string_match = "{\"safe_regex\":{\"google_re2\":{},\"regex\":\"%s\"}}";

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}");
        if (source.contain("$.corsPolicy.allowOrigin")) {
            builder.createOrUpdateValue("$", "allow_origin", source.getValue("$.corsPolicy.allowOrigin", List.class));
        }
        /**
         * https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/route/v3/route_components.proto#config-route-v3-corspolicy
         */
        if (source.contain("$.corsPolicy.allowOriginRegex")) {
            builder.createOrUpdateJson("$", "cors", "{}");
            builder.createOrUpdateJson("$.cors", "allow_origin_string_match", "[]");
            List<String> value = source.getValue("$.corsPolicy.allowOriginRegex", List.class);
            value.forEach(item -> {
                builder.addJsonElement("$.cors.allow_origin_string_match", String.format(allow_origin_string_match, item + "|"));
            });
        }
        if (source.contain("$.corsPolicy.allowMethods")) {
            String allowMethods = String.join(",", source.getValue("$.corsPolicy.allowMethods", List.class));
            builder.createOrUpdateValue("$", "allow_methods", allowMethods);
        }
        if (source.contain("$.corsPolicy.allowHeaders")) {
            String allowHeaders = String.join(",", source.getValue("$.corsPolicy.allowHeaders", List.class));
            builder.createOrUpdateValue("$", "allow_headers", allowHeaders);
        }
        if (source.contain("$.corsPolicy.exposeHeaders")) {
            String exposeHeaders = String.join(",", source.getValue("$.corsPolicy.exposeHeaders", List.class));
            builder.createOrUpdateValue("$", "expose_headers", exposeHeaders);
        }
        if (source.contain("$.corsPolicy.maxAge")) {
            builder.createOrUpdateValue("$", "max_age", source.getValue("$.corsPolicy.maxAge", String.class));
        }
        if (source.contain("$.corsPolicy.allowCredentials")) {
            builder.createOrUpdateValue("$", "allow_credentials", source.getValue("$.corsPolicy.allowCredentials", Boolean.class));
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
