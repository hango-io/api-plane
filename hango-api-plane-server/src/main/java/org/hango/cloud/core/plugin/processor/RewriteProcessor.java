package org.hango.cloud.core.plugin.processor;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class RewriteProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RewriteProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        String formatter = "{\n" +
                "    \"config\": {\n" +
                "        \"decoder_rewriters\": {\n" +
                "            \"extractors\": {\n" +
                "                \"sub-path\": {\n" +
                "                    \"path\": {},\n" +
                "                    \"regex\": \"%s(.*)\",\n" +
                "                    \"group\": 1\n" +
                "                }\n" +
                "            },\n" +
                "            \"rewriters\": [\n" +
                "                {\n" +
                "                    \"path\": {},\n" +
                "                    \"update\": \"%s{{sub-path}}\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}";
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        String rewriteRegex = source.getValue("rewrite_regex");
        String target = source.getValue("target");
        if (StringUtils.isEmpty(target)){
            target = "";
        }
        PluginGenerator builder = PluginGenerator.newInstance(String.format(formatter, rewriteRegex, target));
        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withContent(builder.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_API)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }
}
