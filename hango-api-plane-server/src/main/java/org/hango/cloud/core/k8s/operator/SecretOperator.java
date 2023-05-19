package org.hango.cloud.core.k8s.operator;

import io.fabric8.kubernetes.api.model.Secret;
import org.apache.commons.collections.MapUtils;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.springframework.stereotype.Component;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
@Component
public class SecretOperator implements k8sResourceOperator<Secret> {

    @Override
    public Secret merge(Secret old, Secret fresh) {
        return fresh;
    }

    @Override
    public Secret subtract(Secret old, String value) {
        //do nothing
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.Secret.name().equals(name);
    }

    @Override
    public boolean isUseless(Secret secret) {
        return secret == null || MapUtils.isEmpty(secret.getData());
    }
}
