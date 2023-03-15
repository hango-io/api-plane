package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.EnvoyFilterOrder;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.core.template.TemplateConst.*;

/**
 * @author xin li
 * @date 2022/5/16 11:02
 */
public class EnvoyFilterOrderDataHandler implements DataHandler<EnvoyFilterOrder> {

    private static final String DEFAULT_ENVOY_FILTER_NAME = "grpc-envoy-filter";

    @Override
    public List<TemplateParams> handle(EnvoyFilterOrder envoyFilterOrder) {
        String name = getDefaultEnvoyFilterName(envoyFilterOrder);
        TemplateParams efParams = TemplateParams.instance()
                .put(ENVOY_FILTER_NAME, name)
                .put(ENVOY_FILTER_NAMESPACE, envoyFilterOrder.getNamespace())
                .put(ENVOY_FILTER_WORKLOAD_LABELS, envoyFilterOrder.getWorkloadSelector().getLabelsMap());
        if (!CollectionUtils.isEmpty(envoyFilterOrder.getConfigPatches())) {
            efParams.put(ENVOY_FILTER_FILTERS, envoyFilterOrder.getConfigPatches());
        }
        return Collections.singletonList(efParams);
    }


    @SuppressWarnings("unused")
    private String getDefaultEnvoyFilterName(EnvoyFilterOrder envoyFilterOrder) {
        String name = CollectionUtils.isEmpty(envoyFilterOrder.getWorkloadSelector().getLabelsMap()) ? DEFAULT_ENVOY_FILTER_NAME : joinLabelMap(envoyFilterOrder.getWorkloadSelector().getLabelsMap());
        name = name.replaceAll("_", "-") + "-envoy-filter";
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
        List<String> labels = labelMap.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue()).collect(Collectors.toList());
        return String.join("-", labels);
    }
}
