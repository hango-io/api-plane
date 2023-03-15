package org.hango.cloud.core.plugin.processor;

import com.google.common.collect.Lists;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.PluginMapping;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.CommonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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
        String kind = rg.getValue("$.kind", String.class);
        PluginMapping mapping = PluginMapping.getBymappingName(kind);
        holder = getProcessor(mapping.getProcessorClass()).process(plugin,serviceInfo);
        if (StringUtils.hasText(mapping.getName())){
            if (PluginMapping.ianus_router.getMappingName().equals(kind)) {
                coverToExtensionPlugin(holder, mapping.getName(), true, "ROOT");
            } else {
                coverToExtensionPlugin(holder, mapping.getName());
            }
        }
        return holder;
    }

    public void coverToExtensionPlugin(FragmentHolder holder, String name) {
       coverToExtensionPlugin(holder, name, false, null);
    }

    private void coverToExtensionPlugin(FragmentHolder holder, String name, boolean directPatch, String field) {
        if (Objects.nonNull(holder.getVirtualServiceFragment())) {
            PluginGenerator source = PluginGenerator.newInstance(holder.getVirtualServiceFragment().getContent(), ResourceType.YAML);
            PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", "inline", "{}");

            builder.createOrUpdateJson("$.inline", "settings", source.jsonString());
            builder.createOrUpdateValue("$", "enable", true);
            builder.createOrUpdateJson("$", "listenerType", "Gateway");
            if (directPatch){
                builder.createOrUpdateJson("$.inline", "directPatch", "true");
                builder.createOrUpdateJson("$.inline", "fieldPatchTo", StringUtils.isEmpty(field) ? "route" : field);
            }
            holder.getVirtualServiceFragment().setContent(builder.yamlString());
            logger.info("Extension plugin: [{}]", builder.yamlString());
        }
    }


    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = Lists.newArrayList();
        List<String> luaPlugins = plugins.stream().filter(CommonUtil::isLuaPlugin).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(luaPlugins)) {
            List<FragmentHolder> luaHolder = getProcessor("RestyProcessor").process(luaPlugins, serviceInfo);
            holders.addAll(luaHolder);
        }

        List<String> notLuaPlugins = plugins.stream().filter(item -> !CommonUtil.isLuaPlugin(item)).collect(Collectors.toList());

        List<FragmentHolder> notLuaHolder = notLuaPlugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
        holders.addAll(notLuaHolder);

        // 根据租户将插件分类
        MultiValueMap<String, FragmentWrapper> xUserMap = new LinkedMultiValueMap<>();
        // 一个租户下最多配置一个限流插件
        Map<String, FragmentWrapper> sharedConfigMap = new LinkedHashMap<>();
        Map<String, List<FragmentWrapper>> smartLimiterMap = new LinkedHashMap<>();
        holders.forEach(holder -> {
            FragmentWrapper wrapper = holder.getVirtualServiceFragment();
            FragmentWrapper sharedConfig = holder.getSharedConfigFragment();
            List<FragmentWrapper> smartLimiterList = holder.getSmartLimiterFragment();
            if (wrapper == null) return;
            String xUserId = wrapper.getXUserId();
            String xUser;
            if (StringUtils.isEmpty(xUserId)) {
                xUser = "NoneUser";
            } else {
                xUser = xUserId;
            }
            xUserMap.add(xUser, wrapper);
            if (Objects.nonNull(sharedConfig)) {
                sharedConfigMap.put(xUser, wrapper);
            }
            if (CollectionUtils.isNotEmpty(smartLimiterList)) {
                smartLimiterMap.put(xUser, smartLimiterList);
            }

        });
        List<FragmentHolder> ret = new ArrayList<>();
        for (Map.Entry<String, List<FragmentWrapper>> userMap : xUserMap.entrySet()) {
            PluginGenerator builder = PluginGenerator.newInstance("{\"ext\":[]}");
            for (FragmentWrapper wrapper : userMap.getValue()) {
                PluginGenerator source = PluginGenerator.newInstance(wrapper.getContent(), ResourceType.YAML);
                builder.addJsonElement("$.ext", source.jsonString());
            }
            String xUserId = "NoneUser".equals(userMap.getKey()) ? null : userMap.getKey();
            FragmentHolder holder = new FragmentHolder();
            FragmentWrapper wrapper = new FragmentWrapper.Builder()
                    .withContent(builder.yamlString())
                    .withResourceType(K8sResourceEnum.VirtualService)
                    .withFragmentType(FragmentTypeEnum.VS_API)
                    .withXUserId(xUserId)
                    .build();
            holder.setVirtualServiceFragment(wrapper);
            if (sharedConfigMap.containsKey(userMap.getKey())) {
                holder.setSharedConfigFragment(sharedConfigMap.get(userMap.getKey()));
            }
            if (smartLimiterMap.containsKey(userMap.getKey())) {
                holder.setSmartLimiterFragment(smartLimiterMap.get(userMap.getKey()));
            }
            ret.add(holder);
        }
        return ret;
    }
}
