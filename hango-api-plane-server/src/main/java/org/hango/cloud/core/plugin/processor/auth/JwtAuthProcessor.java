package org.hango.cloud.core.plugin.processor.auth;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.core.plugin.processor.AbstractSchemaProcessor;
import org.hango.cloud.core.plugin.processor.SchemaProcessor;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * JWT认证插件处理器
 *
 * @author yutao
 * @since 2022.8.18
 */
@Component
public class JwtAuthProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final String PROVIDER_FORWARD_AND_ALLOW_MISSING = "requirement_forward_and_allow_missing";
    private static final String PROVIDER_NOT_FORWARD_AND_DENY_MISSING = "requirement_not_forward_and_deny_missing";
    private static final String PROVIDER_NOT_FORWARD_AND_ALLOW_MISSING = "requirement_not_forward_and_allow_missing";
    private static final String PROVIDER_FORWARD_AND_DENY_MISSING = "requirement_forward_and_deny_missing";

    @Override
    public String getName() {
        return "JwtAuth";
    }

    /**
     * 全局 provider:
     * 1. provider_forward_and_allow_missing
     * 2. provider_not_forward_and_deny_missing
     * 3. provider_not_forward_and_allow_missing
     * 4. provider_forward_and_deny_missing
     *
     * @param plugin      插件信息
     * @param serviceInfo 服务信息
     * @return 插件资源
     */
    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}");

        // 设置默认值，默认透传JWT
        Boolean isForwardJwt = true;
        // 设置默认值，默认允许匿名访问
        Boolean allowMissing = true;
        if (source.contain("$.forward")) {
            isForwardJwt = source.getValue("$.forward", Boolean.class);
        }
        if (source.contain("$.allow-missing")) {
            allowMissing = source.getValue("$.allow-missing", Boolean.class);
        }

        String providerKey;
        if (isForwardJwt && allowMissing) {
            providerKey = PROVIDER_FORWARD_AND_ALLOW_MISSING;
        } else if (!isForwardJwt && !allowMissing) {
            providerKey = PROVIDER_NOT_FORWARD_AND_DENY_MISSING;
        } else if (!isForwardJwt && allowMissing) {
            providerKey = PROVIDER_NOT_FORWARD_AND_ALLOW_MISSING;
        } else {
            providerKey = PROVIDER_FORWARD_AND_DENY_MISSING;
        }

        builder.createOrUpdateJson("$", "requirement_name", providerKey);

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
