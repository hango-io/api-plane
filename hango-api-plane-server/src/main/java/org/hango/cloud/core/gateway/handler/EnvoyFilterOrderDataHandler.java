package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.EnvoyFilterOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hango.cloud.core.template.TemplateConst.*;
import static org.hango.cloud.service.impl.GatewayServiceImpl.GW_CLUSTER;

/**
 * @author xin li
 * @date 2022/5/16 11:02
 */
public class EnvoyFilterOrderDataHandler implements DataHandler<EnvoyFilterOrder> {


    @Override
    public List<TemplateParams> handle(EnvoyFilterOrder envoyFilterOrder) {

        TemplateParams efParams = TemplateParams.instance()
                .put(ENVOY_FILTER_NAME, envoyFilterOrder.getName())
                .put(ENVOY_FILTER_NAMESPACE, envoyFilterOrder.getNamespace())
                .put(ENVOY_FILTER_WORKLOAD_LABELS, Collections.singletonMap(GW_CLUSTER, envoyFilterOrder.getGwCluster()));
        if (!CollectionUtils.isEmpty(envoyFilterOrder.getConfigPatches())) {
            List<String> configPatch = envoyFilterOrder.getConfigPatches().stream().filter(StringUtils::hasText).collect(Collectors.toList());
            efParams.put(ENVOY_FILTER_FILTERS, configPatch);
        }
        return Collections.singletonList(efParams);
    }
}
