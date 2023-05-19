package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author xin li
 * @date 2022/5/18 20:15
 */
@Component
public class EnvoyFilterOperator implements k8sResourceOperator<K8sTypes.EnvoyFilter> {
    @Override
    public K8sTypes.EnvoyFilter merge(K8sTypes.EnvoyFilter old, K8sTypes.EnvoyFilter fresh) {
        return fresh;
    }

    @Override
    public K8sTypes.EnvoyFilter subtract(K8sTypes.EnvoyFilter old, String value) {
        old.setSpec(null);
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.EnvoyFilter.name().equals(name);
    }

    @Override
    public boolean isUseless(K8sTypes.EnvoyFilter envoyFilter) {
        return envoyFilter.getSpec() == null || CollectionUtils.isEmpty(envoyFilter.getSpec().getConfigPatchesList());
    }
}
