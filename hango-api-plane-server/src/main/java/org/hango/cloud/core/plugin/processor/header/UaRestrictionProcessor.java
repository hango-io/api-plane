package org.hango.cloud.core.plugin.processor.header;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class UaRestrictionProcessor extends HeaderRestrictionProcessor{

    @Override
    public String getName() {
        return "UaRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        setPluginHeader("User-Agent");
        return super.process(plugin, serviceInfo);
    }
}
