package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 路由插件的转换processor
 *
 **/
@Component
public class RouteProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "RouteProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}");
        builder.createOrUpdateJson("$","direct_response", "{}");
        List<Object> plugins = source.getValue("$.rule");
        //兼容老版本路由插件，当前版本只有return插件
        ResourceGenerator rg = ResourceGenerator.newInstance(plugins.get(0), ResourceType.OBJECT, editorContext);
        builder.createOrUpdateValue("$.direct_response", "status", rg.getValue("$.action.return_target.code", Integer.class));
        builder.createOrUpdateJson("$.direct_response", "body", "{}");
        builder.createOrUpdateValue("$.direct_response.body", "inline_string", rg.getValue("$.action.return_target.body", String.class));
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
