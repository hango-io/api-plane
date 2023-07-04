package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("java:S1602")
public class LuaProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    private AggregateExtensionProcessor extensionProcessor;

    @Override
    public String getName() {
        return "LuaProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}", ResourceType.JSON);
        String kind = source.getValue("$.kind", String.class);
        Object config = source.getValue("$.config");
        builder.createOrUpdateValue("$", "config", config);
        if ("trace".equals(kind)) {
            kind = "neTraceFileLog";
        }
        builder.createOrUpdateValue("$", "name", kind);

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(builder.yamlString())
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .build();
        holder.setGatewayPluginsFragment(wrapper);
        return holder;
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());

        PluginGenerator builder = PluginGenerator.newInstance("{\"plugins\":[]}");
        holders.forEach(item -> {
            builder.addElement("$.plugins",
            PluginGenerator.newInstance(item.getGatewayPluginsFragment().getContent(), ResourceType.YAML).getValue("$"));
        });


        List<FragmentHolder> ret = new ArrayList<>();
        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(builder.yamlString())
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .build();
        holder.setGatewayPluginsFragment(wrapper);
        extensionProcessor.coverToExtensionPlugin(holder, "proxy.filters.http.rider", false, null);
        ret.add(holder);
        return ret;
    }
}
