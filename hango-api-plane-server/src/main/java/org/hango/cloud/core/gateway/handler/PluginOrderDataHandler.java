package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.PluginOrder;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginOrderDataHandler implements DataHandler<PluginOrder> {

    private static final String DEFAULT_PLUGIN_MANAGER_NAME = "qz-global";

    @Override
    public List<TemplateParams> handle(PluginOrder po) {

        String name = getDefaultPluginManagerName(po.getGatewayLabels());

        TemplateParams pmParams = TemplateParams.instance()
                .put(TemplateConst.PLUGIN_MANAGER_NAME, name)
                .put(TemplateConst.NAMESPACE, po.getNamespace())
                .put(TemplateConst.PLUGIN_MANAGER_WORKLOAD_LABELS, po.getGatewayLabels())
                .put(TemplateConst.PLUGIN_MANAGER_PLUGINS, po.getPlugins());
        return Arrays.asList(pmParams);
    }

    private String getDefaultPluginManagerName(Map<String, String> label) {
        String name = CollectionUtils.isEmpty(label) ? DEFAULT_PLUGIN_MANAGER_NAME : joinLabelMap(label);
        name = name.replaceAll("_", "-");
        return name;
    }

    /**
     * 将map中的key value用 "-" 连接
     * 比如 k1-v1-k2-v2
     *
     * @param labelMap
     * @return
     */
    private String joinLabelMap(Map<String, String> labelMap) {

        List<String> labels = labelMap.entrySet().stream()
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.toList());
        return String.join("-", labels);
    }

}
