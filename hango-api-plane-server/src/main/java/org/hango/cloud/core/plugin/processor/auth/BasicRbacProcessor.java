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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static org.hango.cloud.util.Const.*;

/**
 * 基础鉴权插件处理器
 *
 * @author yutao
 * @since 2022.8.18
 */
@Component
public class BasicRbacProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "BasicRbac";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        // 默认支持ALLOW策略
        String action = "ALLOW";
        // 默认支持JWT类型鉴权，其他类型鉴权适配见方法 convertToEnvoyFilter(String authType)
        String filter = RBAC_IDENTITY_FILTER;
        PluginGenerator resultBuilder = PluginGenerator.newInstance("{\"rbac\":{\"rules\":{\"action\":\"" + action + "\",\"policies\":{\"gw-strategy\":{\"permissions\":[{\"any\":true}],\"principals\":[{\"or_ids\":{\"ids\":[]}}]}}}}}");

        Map<String, List<Map<String, String>>> principalList;
        if (source.contain("$.principal_list")) {
            principalList = source.getValue("$.principal_list", Map.class);
        } else {
            throw new RuntimeException("[basic-rbac plugin] 错误的插件结构，缺少principal_list根节点");
        }
        List<Map<String, String>> authGroupList = principalList.get("auth_group");
        for (Map<String, String> authGroup : authGroupList) {
            // 后续根据authGroup中的auth_type来确认此身份对应的凭证类型
            if (StringUtils.hasText(authGroup.get("auth_type"))) {
                filter = convertToEnvoyFilter(authGroup.get("auth_type"));
            }
            PluginGenerator metadataBuilder = PluginGenerator.newInstance("{\"metadata\":{\"filter\":\"" + filter + "\",\"path\":[{\"key\":\"payload\"},{\"key\":\"name\"}],\"value\":{\"string_match\":{\"exact\":\"" + authGroup.get("name") + "\"}}}}");
            resultBuilder.addJsonElement("$.rbac.rules.policies.gw-strategy.principals[0].or_ids.ids", metadataBuilder.jsonString());
        }

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(resultBuilder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    /**
     * 后续扩展认证插件类型在此扩
     * 例如：添加了一种简单认证，则平台处新增一种authType，对应envoy一种filter
     *
     * @param authType 认证类型
     * @return envoy filter全名
     */
    private String convertToEnvoyFilter(String authType) {
        if (StringUtils.isEmpty(authType)) {
            throw new RuntimeException("基础鉴权插件转换异常，authType为空！");
        }
        String filter;
        switch (authType) {
            case AUTH_JWKS:
                filter = JWT_FILTER;
                break;
            default:
                throw new RuntimeException("没有对应的filter类型，authType:" + authType);
        }
        return filter;
    }
}
