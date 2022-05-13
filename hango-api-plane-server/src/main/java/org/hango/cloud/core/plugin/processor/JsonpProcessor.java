package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/11
 **/
@Component
public class JsonpProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "JsonpProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        String callback = source.getValue("callback");
        PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"request_transformations\":[{\"transformation_template\":{\"extractors\":{\"call-back\":{\"query_param\":\"%s\",\"set_to_context\":true}},\"parse_body_behavior\":\"DontParse\"}}],\"response_transformations\":[{\"transformation_template\":{\"extractors\":{\"all-body\":{\"body\":{},\"regex\":\"([\\\\s\\\\S]*)\",\"subgroup\":1}},\"body\":{\"text\":\"{{call-back}}({{all-body}})\"},\"parse_body_behavior\":\"DontParse\"},\"conditions\":[{\"context\":{\"request_method\":\"GET\",\"context_conditions\":[{\"key\":\"call-back\",\"op\":\"IsNotNull\"}]},\"headers\":[{\"name\":\"Content-Type\",\"regex_match\":\".*application/json.*\"},{\"name\":\"content-type\",\"regex_match\":\".*application/json.*\"}]}]}]}", callback));
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
