package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class FlowLimitProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "FlowLimitProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        int percent = Integer.parseInt(rg.getValue("$.limit_percent", String.class));
        PluginGenerator ret = PluginGenerator.newInstance(String.format("{\"abort\":{\"http_status\":429,\"percentage\":{\"denominator\":\"MILLION\",\"numerator\":%s}}}", percent * 10000));

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .withContent(ret.yamlString())
                .build();
        fragmentHolder.setGatewayPluginsFragment(wrapper);
        return fragmentHolder;
    }
}
