package org.hango.cloud.core.plugin.processor.auth;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.processor.AbstractSchemaProcessor;
import org.hango.cloud.core.plugin.processor.SchemaProcessor;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

public abstract class AuthProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private String result_cache = "authz_result_cache";
    private String result_cache_key = "result_cache_key";
    private String result_cache_ttl = "result_cache_ttl";
    private String cacheKey = "$." + result_cache + "." + result_cache_key;
    private String cacheTtl = "$." + result_cache + "." + result_cache_ttl;

    private static final String PARAMETER_NAME = "$.appNameSetting.parameterName";

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"need_authorization\":false, \"missing_auth_allow\":\"false\"}");
        String kind = source.getValue("$.kind", String.class);

        switch (kind) {
            case "simple-auth":
                builder.createOrUpdateJson("$", "authn_policy_name", AuthTypeEnum.SimpleAuth.getAuth_type());
                break;
            case "oauth2-auth": default:
                builder.createOrUpdateJson("$", Objects.requireNonNull(AuthTypeEnum.getAuthTypeEnum(kind)).getAuth_type(), "{}");
                break;
        }

        // 旧版本super_auth插件有鉴权开关功能，保留鉴权配置开关
        Boolean useAuthz = source.getValue("$.useAuthz", Boolean.class);
        if (useAuthz != null) {
            builder.updateValue("$.need_authorization", useAuthz);
        }
        Boolean failureAuthAllow = source.getValue("$.failureAuthAllow", Boolean.class);
        failureAuthAllow = null == failureAuthAllow ? false : failureAuthAllow;
        // 允许携带正确token或不携带token，携带错误token认证不通过
        builder.updateValue("$.missing_auth_allow", failureAuthAllow);

        if (source.contain("$.bufferSetting.maxRequestBytes")) {
            String maxRequestBody = source.getValue("$.bufferSetting.maxRequestBytes", String.class);
            builder.createOrUpdateJson("$", "with_request_body", String.format("{\"max_request_bytes\":\"%s\", \"allow_partial_message\":\"false\"}", maxRequestBody));
        }
        Boolean allowPartialMessage = source.getValue("$.bufferSetting.allowPartialMessage", Boolean.class);

        if (source.contain(PARAMETER_NAME)) {
            String parameterName = source.getValue(PARAMETER_NAME, String.class);
            builder.createOrUpdateJson("$", "authn_policy_config", String.format("{\"token_format\": \"ANY\",\"token_source\": \"%s\",\"token_rename\": \"authorization\",\"send_context\": true}", parameterName));
        }

        builder.updateValue("$.with_request_body.allow_partial_message", allowPartialMessage);

        if (source.contain("$." + result_cache)){
            buildCache(source, builder);
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

    /**
     * Generate auth cache model. In order to improve auth performance.
     * Design: https://kms.netease.com/team/km_qingzhou/article/29870
     * @param source plugin source
     * @param builder plugin builder
     */
    private void buildCache(ResourceGenerator source, ResourceGenerator builder){
        builder.createOrUpdateJson("$", result_cache, "{}");
        if (source.contain(cacheTtl)) {
            builder.createOrUpdateValue("$." + result_cache, result_cache_ttl, source.getValue(cacheTtl, Integer.class) * 1000);
        }
        if (source.contain(cacheKey)){
            createCacheKey(source,builder);
        }
    }

    /**
     * Generate result_cache_key
     * @param source plugin source
     * @param builder plugin builder
     */
    private void createCacheKey(ResourceGenerator source, ResourceGenerator builder) {
        String ignore_case = "ignore_case";
        String header_keys = "headers_keys";
        builder.createOrUpdateJson("$."+ result_cache, result_cache_key, "{}");
        Boolean ignoreCase = source.getValue(cacheKey + "." + ignore_case, Boolean.class);
        if (nonNull(ignoreCase)) {
            builder.createOrUpdateValue(cacheKey, ignore_case, ignoreCase);
        }
        builder.createOrUpdateJson(cacheKey, header_keys, "[]");
        if (source.contain(cacheKey + "." + header_keys)) {

            List<String> headers = source.getValue(cacheKey + "." + header_keys, List.class);
            headers.forEach(item -> {
                builder.addJsonElement(cacheKey + "." + header_keys, item);
            });
        } else {
            String kind = source.getValue("$.kind", String.class);
            switch (kind) {
                case "simple-auth":
                    builder.addJsonElement(cacheKey + "." + header_keys, source.getValue(PARAMETER_NAME, String.class));
                    break;
                case "aksk_authn_type": case "jwt-auth": case "oauth2-auth": default:
                    builder.addJsonElement(cacheKey + "." + header_keys, Objects.requireNonNull(AuthTypeEnum.getAuthTypeEnum(kind)).getCache_key());
                    break;
            }
        }
    }
}
