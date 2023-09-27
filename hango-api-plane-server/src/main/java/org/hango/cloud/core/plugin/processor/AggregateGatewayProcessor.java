package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.meta.enums.PluginMappingEnum;
import org.hango.cloud.meta.enums.PluginProcessorEnum;
import org.hango.cloud.meta.enums.PluginScopeTypeEnum;
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
@SuppressWarnings({"java:S3740","java:S1192"})
public class AggregateGatewayProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AggregateGatewayProcessor.class);

    @Autowired
    protected List<SchemaProcessor> processorList;

    @Autowired
    GlobalConfig globalConfig;

    public static final String INLINE = "$.inline";

    //虚构的插件名称
    public static final String FAKE_NAME = "proxy.filters.http.fake";

    public FragmentHolder process(String plugin, String pluginScope) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        //插件process匹配
        PluginProcessorEnum processor = PluginProcessorEnum.get(CommonUtil.getPluginName(rg));
        if (processor == null){
            logger.error("Unsupported plugin : [{}]", plugin);
            throw new ApiPlaneException(String.format("Unsupported plugin kind: [%s]", plugin));
        }
        //插件处理
        FragmentHolder holder = getProcessor(processor.getProcessorName()).process(plugin, new ServiceInfo());
        //后置处理
        postHandle(rg, holder, pluginScope);
        return holder;
    }

    private void postHandle(PluginGenerator rg, FragmentHolder holder, String pluginScope){
        String pluginName = CommonUtil.getPluginName(rg);
        String kind = CommonUtil.getKind(rg);
        //ianus_router插件
        if (PluginProcessorEnum.ianus_router.getPluginName().equals(pluginName)){
            convertIanusRouter(holder);
            return;
        }
        PluginMappingEnum pluginMapping = PluginMappingEnum.getByPluginName(pluginName);
        if (pluginMapping == null){
            logger.error("Unsupported plugin : [{}]", pluginName);
            throw new ApiPlaneException(String.format("Unsupported plugin kind: [%s]", pluginName));
        }
        //网关级插件
        if (PluginScopeTypeEnum.isGatewayPlugin(pluginScope)){
            covert2BasePlugin(holder, getFilterName(pluginMapping, kind), kind);
            return;
        }
        covert2ExtensionPlugin(holder, getFilterName(pluginMapping, kind), pluginMapping.getTypeUrl(), CommonUtil.getPluginType(rg));
    }

    private String getFilterName(PluginMappingEnum pluginMapping, String kind){
        switch (pluginMapping){
            case RIDER:
            case WASM:
                return kind;
            default:
                return pluginMapping.getFilterName();
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

    private void convertIanusRouter(FragmentHolder holder) {
        if (Objects.isNull(holder.getGatewayPluginsFragment())) {
            return;
        }
        PluginGenerator source = PluginGenerator.newInstance(holder.getGatewayPluginsFragment().getContent(), ResourceType.YAML);
        //请求中断插件envoy不是通过插件实现，插件名称无效
        PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", FAKE_NAME));
        builder.createOrUpdateValue("$", "enable", true);
        builder.createOrUpdateJson("$", "listenerType", "Gateway");
        builder.createOrUpdateJson("$", "inline", "{}");
        builder.createOrUpdateJson(INLINE, "settings", source.jsonString());
        builder.createOrUpdateJson(INLINE, "directPatch", "true");
        builder.createOrUpdateJson(INLINE, "fieldPatchTo", "ROOT");
        holder.getGatewayPluginsFragment().setContent(builder.yamlString());
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

    private SchemaProcessor getProcessor(String name) {
        if (CollectionUtils.isEmpty(processorList)) throw new ApiPlaneException("The list of processors is empty");
        for (SchemaProcessor item : processorList) {
            if (name.equalsIgnoreCase(item.getName())) return item;
        }
        throw new ApiPlaneException(String.format("Processor [%s] could not be found", name));
    }
}
