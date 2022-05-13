package org.hango.cloud.core.plugin.processor;

import com.google.common.collect.Lists;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.CommonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/1/17
 **/
@Component
public class AggregateGatewayPluginProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "AggregateGatewayPluginProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder holder = getProcessor("AggregateExtensionProcessor").process(plugin, serviceInfo);
        convertToGatewayPlugin(holder);
        return holder;
    }

    private void convertToGatewayPlugin(FragmentHolder holder) {
        if (Objects.nonNull(holder.getVirtualServiceFragment())) {
            holder.setGatewayPluginsFragment(holder.getVirtualServiceFragment());
            return;
        }
        if (Objects.nonNull(holder.getSharedConfigFragment())) {
            holder.setGatewayPluginsFragment(holder.getSharedConfigFragment());
            return;
        }
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> ret = Lists.newArrayList();

        List<String> luaPlugins = plugins.stream().filter(CommonUtil::isLuaPlugin).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(luaPlugins)) {
            List<FragmentHolder> luaHolder = getProcessor("RestyProcessor").process(luaPlugins, serviceInfo);
            luaHolder.forEach(this::convertToGatewayPlugin);
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
