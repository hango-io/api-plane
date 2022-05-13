package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/2/10
 **/
@Component
public class TraceProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "TraceProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator builder = PluginGenerator.newInstance("{\"plugins\":[{\"name\":\"neTraceFileLog\"}]}", ResourceType.JSON);
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        Object config = source.getValue("$.config");
        builder.createOrUpdateValue("$.plugins[0]", "config", config);

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withContent(builder.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_API)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }
}
