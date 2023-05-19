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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 21.0.x版本认证处理器，为了兼容老版本认真插件升级，在22.0.x版本新增PreviousVersionAuthProcessor
 * 21.0.x版本envoy认证插件名: com.netease.superauthz; 22.0.x版本envoy认证插件名: proxy.filters.http.super_authz
 * 21.0.x版本认证插件在前端仅一个; 22.0.x版本在前端分3个插件: JWT、oauth2-auth、sign-auth
 * 升级迁移策略: 后续对于在21.0.x版本配置了认证插件的情况，22.0.x版本也会有相同的插件，后续用户需要手动将该旧插件配置迁移至22.0.x版本的3个新插件
 *
 * @author yutao04
 * @date 2022/6/14 9:56
 */
@Component
public class PreviousVersionAuthProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "PreviousVersionSuperAuth";
    }

    private String result_cache = "authz_result_cache";
    private String result_cache_key = "result_cache_key";
    private String result_cache_ttl = "result_cache_ttl";
    private String cacheKey = "$." + result_cache + "." + result_cache_key;
    private String cacheTtl = "$." + result_cache + "." + result_cache_ttl;
    public static final String AKSK_AUTHN_TYPE = "aksk_authn_type";
    public static final String JWT_AUTHN_TYPE = "jwt_authn_type";
    public static final String OUTH2_AUTH_TYPE = "oauth2_authn_type";

    Map<String,String> authnType_to_cacheKey = new HashMap<String, String>(){{
        put(AKSK_AUTHN_TYPE, "x-nsf-accesskey");
        put(JWT_AUTHN_TYPE, "authority");
        put(OUTH2_AUTH_TYPE, "authority");
    }};

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"need_authorization\":\"false\", \"failure_auth_allow\":\"false\"}");
        String authType = source.getValue("$.authnType", String.class);
        if (AKSK_AUTHN_TYPE.equals(authType)) {
            builder.createOrUpdateJson("$", AKSK_AUTHN_TYPE, "{}");
        } else if (JWT_AUTHN_TYPE.equals(authType)){
            builder.createOrUpdateJson("$", JWT_AUTHN_TYPE, "{}");
        }else {
            builder.createOrUpdateJson("$", OUTH2_AUTH_TYPE, "{}");
        }

        builder.updateValue("$.need_authorization", source.getValue("$.useAuthz", Boolean.class));
        Boolean failureAuthAllow = source.getValue("$.failureAuthAllow", Boolean.class);
        failureAuthAllow = null == failureAuthAllow ? false : failureAuthAllow;
        builder.updateValue("$.failure_auth_allow", failureAuthAllow);

        if (source.contain("$.bufferSetting.maxRequestBytes")) {
            String maxRequestBody = source.getValue("$.bufferSetting.maxRequestBytes", String.class);
            builder.createOrUpdateJson("$", "with_request_body", String.format("{\"max_request_bytes\":\"%s\", \"allow_partial_message\":\"false\"}", maxRequestBody));
        }
        Boolean allowPartialMessage = source.getValue("$.bufferSetting.allowPartialMessage", Boolean.class);
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

            List<String> headers = source.getValue(cacheKey + "." + header_keys , List.class);
            headers.forEach(item -> {
                builder.addJsonElement(cacheKey + "." + header_keys, item);
            });
        }else {
            String authType = source.getValue("$.authnType", String.class);
            builder.addJsonElement(cacheKey + "." + header_keys, authnType_to_cacheKey.get(authType));
        }
    }
}
