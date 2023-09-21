package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.PluginMapping;
import org.hango.cloud.meta.PluginScopeTypeEnum;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Component
@SuppressWarnings("java:S3740")
public class AggregateGatewayProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AggregateGatewayProcessor.class);

    @Autowired
    protected List<SchemaProcessor> processorList;

    @Autowired
    GlobalConfig globalConfig;

    public static final String INLINE = "$.inline";

    public FragmentHolder process(String plugin, String pluginScope) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        //插件process匹配
        PluginMapping mapping = PluginMapping.getBymappingName(CommonUtil.getKind(rg));
        if (mapping == null){
            logger.error("Unsupported plugin : [{}]", plugin);
            throw new ApiPlaneException(String.format("Unsupported plugin kind: [%s]", plugin));
        }
        //插件处理
        FragmentHolder holder = getProcessor(mapping.getProcessorName()).process(plugin, new ServiceInfo());
        //插件配置后置处理
        postHandle(mapping, rg.getValue("$.kind", String.class), holder, pluginScope);
        return holder;
    }



    private void postHandle(PluginMapping mapping, String kind, FragmentHolder holder, String pluginScope){
        String pluginType = getPluginType(mapping);
        //网关级插件
        if (PluginScopeTypeEnum.isGatewayPlugin(pluginScope)){
            covert2BasePlugin(holder, PluginMapping.getName(kind), pluginType);
            return;
        }
        //ianus_router插件
        if (PluginMapping.ianus_router.equals(mapping)){
            convertIanusRouter(holder, PluginMapping.getName(kind), mapping.getTypeUrl());
            return;
        }
        covert2ExtensionPlugin(holder, getPluginName(mapping, kind), mapping.getTypeUrl(), pluginType);
    }

    private String getPluginName(PluginMapping mapping, String kind){
        switch (mapping){
            case lua:
                return globalConfig.getResourceNamespace() + "." + kind + ".rider";
            case wasm:
                return globalConfig.getResourceNamespace() + "." + kind;
            default:
                return mapping.getName();
        }
    }

    private void covert2ExtensionPlugin(FragmentHolder holder, String name, String typeUrl, String pluginType) {
        if (Objects.nonNull(holder.getGatewayPluginsFragment())) {
            PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);
            PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", pluginType, "{}");
            if (StringUtils.hasText(typeUrl)){
                builder.createOrUpdateJson("$", "typeUrl", typeUrl);
            }
            builder.createOrUpdateJson("$." + pluginType, "settings", source.jsonString());
            builder.createOrUpdateValue("$", "enable", true);
            builder.createOrUpdateJson("$", "listenerType", "Gateway");
            holder.getGatewayPluginsFragment().setContent(builder.yamlString());
            logger.info("Extension plugin: [{}]", builder.yamlString());
        }
    }

    private void convertIanusRouter(FragmentHolder holder, String name, String typeUrl) {
        if (Objects.isNull(holder.getGatewayPluginsFragment())) {
            return;
        }
        PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);
        PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
        builder.createOrUpdateValue("$", "enable", true);
        builder.createOrUpdateJson("$", "listenerType", "Gateway");
        builder.createOrUpdateJson("$", "inline", "{}");
        if (StringUtils.hasText(typeUrl)){
            builder.createOrUpdateJson("$", "typeUrl", typeUrl);
        }
        builder.createOrUpdateJson(INLINE, "settings", source.jsonString());
        builder.createOrUpdateJson(INLINE, "directPatch", "true");
        builder.createOrUpdateJson(INLINE, "fieldPatchTo", "ROOT");
        holder.getGatewayPluginsFragment().setContent(builder.yamlString());
        logger.info("ians rouyer plugin: [{}]", builder.yamlString());
    }

    private void covert2BasePlugin(FragmentHolder holder, String name, String type) {
        if (Objects.nonNull(holder.getGatewayPluginsFragment())) {
            PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);
            PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", type, "{}");
            builder.createOrUpdateJson(String.format("$.%s", type), "settings", source.jsonString());
            holder.getGatewayPluginsFragment().setContent(builder.yamlString());
        }
    }

    private String getPluginType(PluginMapping mapping) {
        if (PluginMapping.lua.equals(mapping)) {
            return "rider";
        } else if (PluginMapping.wasm.equals(mapping)) {
            return "wasm";
        } else {
            return "inline";
        }
    }

    private SchemaProcessor getProcessor(String name) {
        if (CollectionUtils.isEmpty(processorList)) throw new ApiPlaneException("The list of processors is empty");
        for (SchemaProcessor item : processorList) {
            if (name.equalsIgnoreCase(item.getName())) return item;
        }
        throw new ApiPlaneException(String.format("Processor [%s] could not be found", name));
    }
}
