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
        //网关级插件
        if (PluginScopeTypeEnum.isGatewayPlugin(pluginScope)){
            covert2BasePlugin(holder, PluginMapping.getName(kind), getPluginType(mapping));
            return;
        }
        //ianus_router插件
        if (PluginMapping.ianus_router.equals(mapping)){
            covert2ExtensionPlugin(holder, PluginMapping.getName(kind), mapping.getTypeUrl(), true, "ROOT");
            return;
        }
        //lua插件
        if (PluginMapping.lua.equals(mapping)){
            addLuaConfig(holder, PluginMapping.getName(kind));
        }
        //wasm插件
        if (PluginMapping.wasm.equals(mapping)){
            addWasmConfig(holder);
        }
        covert2ExtensionPlugin(holder, getPluginName(mapping, kind), mapping.getTypeUrl(), false, null);
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

    private void addLuaConfig(FragmentHolder holder, String pluginName){
        PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);

        PluginGenerator builder = PluginGenerator.newInstance("{}", ResourceType.JSON);
        builder.createOrUpdateJson("$", "config", source.jsonString());
        builder.createOrUpdateJson("$", "name", pluginName);
        PluginGenerator target = PluginGenerator.newInstance("{\"plugins\":[]}");
        target.addElement("$.plugins", builder.getValue("$"));
        holder.getGatewayPluginsFragment().setContent(target.yamlString());
    }

    private void addWasmConfig(FragmentHolder holder){
        PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);

        PluginGenerator builder = PluginGenerator.newInstance("{}", ResourceType.JSON);
        builder.createOrUpdateJson("$", "configuration", source.jsonString());
        holder.getGatewayPluginsFragment().setContent(builder.yamlString());
    }

    private void covert2ExtensionPlugin(FragmentHolder holder, String name, String typeUrl, boolean directPatch, String field) {
        if (Objects.nonNull(holder.getGatewayPluginsFragment())) {
            PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);
            PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", "inline", "{}");
            if (StringUtils.hasText(typeUrl)){
                builder.createOrUpdateJson("$", "typeUrl", typeUrl);
            }
            builder.createOrUpdateJson(INLINE, "settings", source.jsonString());
            builder.createOrUpdateValue("$", "enable", true);
            builder.createOrUpdateJson("$", "listenerType", "Gateway");
            if (directPatch){
                builder.createOrUpdateJson(INLINE, "directPatch", "true");
                builder.createOrUpdateJson(INLINE, "fieldPatchTo", StringUtils.isEmpty(field) ? "route" : field);
            }
            holder.getGatewayPluginsFragment().setContent(builder.yamlString());
            logger.info("Extension plugin: [{}]", builder.yamlString());
        }
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
