package org.hango.cloud.core.k8s.operator;

import io.fabric8.kubernetes.api.model.Service;
import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.springframework.stereotype.Component;

/**
 * @Author zhufengwei
 * @Date 2023/3/13
 */
@Component
public class KubernetesServiceOperator implements k8sResourceOperator<Service> {
    @Override
    public Service merge(Service old, Service fresh) {
        old.getSpec().setPorts(fresh.getSpec().getPorts());
        return old;
    }

    @Override
    public Service subtract(Service old, String value) {
        //do nothing
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.Service.name().equals(name);
    }

    @Override
    public boolean isUseless(Service service) {
        return service == null || service.getSpec() == null || CollectionUtils.isEmpty(service.getSpec().getPorts());
    }
}
