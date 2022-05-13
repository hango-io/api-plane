package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class IpRestrictionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "IpRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        PluginGenerator ret = PluginGenerator.newInstance("{\"list\":[]}");
        if (Objects.equals("0", rg.getValue("$.type", String.class))) {
            ret.createOrUpdateJson("$", "type", "BLACK");
        } else if (Objects.equals("1", rg.getValue("$.type", String.class))) {
            ret.createOrUpdateJson("$", "type", "WHITE");
        }
        ret.createOrUpdateJson("$.ip_restriction", "type", rg.getValue("$.type", String.class));
        List<String> ips = rg.getValue("$.list[*]");
        for (String ip : ips) {
            ret.addJsonElement("$.list", ip);
        }
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(rg))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(ret.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
