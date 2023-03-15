package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class SoapJsonTranscoderProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "SoapJsonTranscoderProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder holder = new FragmentHolder();
        PluginGenerator total = PluginGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        total.removeElement("$.kind");
        holder.setVirtualServiceFragment(
                new FragmentWrapper.Builder()
                        .withXUserId(getAndDeleteXUserId(total))
                        .withFragmentType(FragmentTypeEnum.VS_API)
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withContent(total.yamlString())
                        .build()
        );
        return holder;
    }
}
