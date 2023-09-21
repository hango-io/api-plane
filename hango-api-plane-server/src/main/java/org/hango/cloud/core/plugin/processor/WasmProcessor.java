package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @Author zhufengwei
 * @Date 2023/8/9
 */
@Component
public class WasmProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo>{

    @Override
    public String getName() {
        return "WasmProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        source.removeElement("$.kind");
        source.removeElement("$.type");
        FragmentHolder holder = new FragmentHolder();
        PluginGenerator builder = PluginGenerator.newInstance("{}", ResourceType.JSON);
        builder.createOrUpdateJson("$", "@type", "type.googleapis.com/google.protobuf.StringValue");
        builder.createOrUpdateValue("$", "value", source.jsonString());
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(builder.yamlString())
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .build();
        holder.setGatewayPluginsFragment(wrapper);

        return holder;
    }
}
