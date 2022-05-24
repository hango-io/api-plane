package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;

import org.springframework.stereotype.Component;

@Component
public class FunctionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final String FUNCTION_TEMPLATE =
            "function envoy_on_request(request_handle)\n" +
                    "  %s\n" +
                    "end\n" +
                    "function envoy_on_response(response_handle)\n" +
                    "  %s\n" +
                    "end\n";

    private static EditorContext editorContext = new EditorContext(new ObjectMapper(),
            new YAMLMapper().configure(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE, true),
            Configuration.defaultConfiguration());

    @Override
    public String getName() {
        return "FunctionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        String request = ((String) rg.getValue("$.envoy_on_request")).replace("\n", "\n  ");
        String response = ((String) rg.getValue("$.envoy_on_response")).replace("\n", "\n  ");
        PluginGenerator ret = PluginGenerator.newInstance("{\"code\":{\"inline_string\": \"\"}}", ResourceType.JSON,
                editorContext);
        ret.createOrUpdateValue("$.code", "inline_string", String.format(FUNCTION_TEMPLATE, request, response));

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
