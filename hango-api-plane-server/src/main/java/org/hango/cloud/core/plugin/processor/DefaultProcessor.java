package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * 直接透传的Processor
 *
 **/
@Component
public class DefaultProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "DefaultProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        if(source.contain("$.kind"))source.removeElement("$.kind");
        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(source.yamlString())
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .build();
        holder.setGatewayPluginsFragment(wrapper);
        return holder;
    }
}
