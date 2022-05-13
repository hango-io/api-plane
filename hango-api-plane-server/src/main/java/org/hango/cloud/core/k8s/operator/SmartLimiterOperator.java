package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
@Component
public class SmartLimiterOperator implements k8sResourceOperator<SmartLimiter> {

    @Override
    public SmartLimiter merge(SmartLimiter old, SmartLimiter fresh) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SmartLimiter subtract(SmartLimiter old, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.SmartLimiter.name().equals(name);
    }

    @Override
    public boolean isUseless(SmartLimiter smartLimiter) {
        return smartLimiter == null ||
                smartLimiter.getSpec() == null ||
                smartLimiter.getSpec().getRatelimitConfig() == null ||
                smartLimiter.getSpec().getRatelimitConfig().getRateLimitConf() == null ||
                CollectionUtils.isEmpty(smartLimiter.getSpec().getRatelimitConfig().getRateLimitConf().getDescriptors());
    }
}
