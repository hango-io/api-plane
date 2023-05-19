package org.hango.cloud.core.gateway.handler;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.PluginOrder;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.core.template.TemplateConst.*;

public class PluginOrderDataHandler implements DataHandler<PluginOrder> {

    private static final String DEFAULT_PLUGIN_MANAGER_NAME = "qz-global";

    @Override
    public List<TemplateParams> handle(PluginOrder po) {

        String name = getDefaultPluginManagerName(po);

        TemplateParams pmParams = TemplateParams.instance()
                .put(PLUGIN_MANAGER_NAME, name)
                .put(NAMESPACE, po.getNamespace())
                .put(PLUGIN_MANAGER_WORKLOAD_LABELS, po.getGatewayLabels())
                .put(PLUGIN_MANAGER_PLUGINS, po.getPlugins());
        return Arrays.asList(pmParams);
    }

    private String getDefaultPluginManagerName(PluginOrder po) {
        String name;
        if (StringUtils.isNotBlank(po.getName())){
            name = po.getName();
        }else {
            name = CollectionUtils.isEmpty(po.getGatewayLabels()) ? DEFAULT_PLUGIN_MANAGER_NAME : joinLabelMap(po.getGatewayLabels());
        }
        return  name.replaceAll("_", "-");
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
