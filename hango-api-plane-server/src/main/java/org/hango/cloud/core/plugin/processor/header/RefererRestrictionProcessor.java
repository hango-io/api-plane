package org.hango.cloud.core.plugin.processor.header;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;
@Component
public class RefererRestrictionProcessor extends HeaderRestrictionProcessor {


    @Override
    public String getName() {
        return "RefererRestrictionProcessor";
    }


    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        setPluginHeader("Referer");
        return super.process(plugin, serviceInfo);
    }

}
