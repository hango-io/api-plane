package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.PluginMapping;
import org.hango.cloud.meta.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static org.hango.cloud.util.Const.LUA;

@Component
public class AggregateExtensionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final Logger logger = LoggerFactory.getLogger(AggregateExtensionProcessor.class);

    @Override
    public String getName() {
        return "AggregateExtensionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        FragmentHolder holder;
        String kind = getKind(rg);
        PluginMapping mapping = PluginMapping.getBymappingName(getKind(rg));
        holder = getProcessor(mapping.getProcessorName()).process(plugin,serviceInfo);
        if (StringUtils.hasText(mapping.getName())){
            if (PluginMapping.ianus_router.getMappingName().equals(kind)) {
                coverToExtensionPlugin(holder, mapping.getName(), true, "ROOT");
            } else {
                coverToExtensionPlugin(holder, mapping.getName(), false, null);
            }
        }
        return holder;
    }

    public void coverToExtensionPlugin(FragmentHolder holder, String name, boolean directPatch, String field) {
        if (Objects.nonNull(holder.getGatewayPluginsFragment())) {
            PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);
            PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", "inline", "{}");

            builder.createOrUpdateJson("$.inline", "settings", source.jsonString());
            builder.createOrUpdateValue("$", "enable", true);
            builder.createOrUpdateJson("$", "listenerType", "Gateway");
            if (directPatch){
                builder.createOrUpdateJson("$.inline", "directPatch", "true");
                builder.createOrUpdateJson("$.inline", "fieldPatchTo", StringUtils.isEmpty(field) ? "route" : field);
            }
            holder.getGatewayPluginsFragment().setContent(builder.yamlString());
            logger.info("Extension plugin: [{}]", builder.yamlString());
        }
    }

    private String getKind(PluginGenerator rg){
        String type = rg.getValue("$.type", String.class);
        if (LUA.equals(type)){
            return LUA;
        }
        return rg.getValue("$.kind", String.class);
    }
}
