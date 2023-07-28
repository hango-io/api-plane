package org.hango.cloud.core.plugin.processor;

import com.google.common.collect.Lists;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AggregateGatewayPluginProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    AggregateExtensionProcessor aggregateExtensionProcessor;

    @Autowired
    LuaProcessor luaProcessor;

    @Override
    public String getName() {
        return "AggregateGatewayPluginProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        return aggregateExtensionProcessor.process(plugin, serviceInfo);
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> ret = Lists.newArrayList();

        List<String> luaPlugins = plugins.stream().filter(CommonUtil::isLuaPlugin).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(luaPlugins)) {
            List<FragmentHolder> luaHolder = luaProcessor.process(luaPlugins, serviceInfo);
            ret.addAll(luaHolder);
        }

        List<String> notLuaPlugins = plugins.stream().filter(item -> !CommonUtil.isLuaPlugin(item)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(notLuaPlugins)) {
            List<FragmentHolder> notLuaHolder = notLuaPlugins.stream()
                    .map(plugin -> process(plugin, serviceInfo))
                    .collect(Collectors.toList());
            ret.addAll(notLuaHolder);
        }

        return ret;
    }
}
