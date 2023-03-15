package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.springframework.stereotype.Component;

@Component
public class ConfigMapOperator implements k8sResourceOperator<ConfigMap> {
    @Override
    public ConfigMap merge(ConfigMap old, ConfigMap fresh) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigMap subtract(ConfigMap old, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.ConfigMap.name().equals(name);
    }

    @Override
    public boolean isUseless(ConfigMap configMap) {
        return configMap == null;
    }
}
