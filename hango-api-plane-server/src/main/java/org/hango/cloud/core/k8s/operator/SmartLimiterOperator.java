package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import slime.microservice.limiter.v1alpha2.SmartLimiter;

/**
 * SmartLimiter资源操作类
 *
 * @author yutao04
 * @since 2022.09.01
 */
@Component
public class SmartLimiterOperator implements k8sResourceOperator<K8sTypes.SmartLimiter> {

    @Override
    public K8sTypes.SmartLimiter merge(K8sTypes.SmartLimiter old, K8sTypes.SmartLimiter fresh) {
        K8sTypes.SmartLimiter latest = new K8sTypes.SmartLimiter();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());
        if (fresh.getMetadata() != null && fresh.getMetadata().getLabels() != null) {
            latest.getMetadata().setLabels(fresh.getMetadata().getLabels());
        }
        SmartLimiter.SmartLimiterSpec.Builder builder = fresh.getSpec().toBuilder();
        latest.setSpec(builder.build());
        return latest;
    }

    @Override
    public K8sTypes.SmartLimiter subtract(K8sTypes.SmartLimiter old, String value) {
        old.setSpec(null);
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.SmartLimiter.name().equals(name);
    }

    /**
     * ① 如下所示，正常的SmartLimiter都存在至少1个descriptor
     * spec:
     *   sets:
     *     _base:
     *       ## descriptor下可存在多个限流策略
     *       descriptor:
     *       - action: ...
     *
     * ② 不存在descriptor的场景下代表SmartLimiter无效，sets下不存在_base节点，则认为需要被删除
     * spec:
     *   sets: `EMPTY`
     *
     * @param smartLimiter
     * @return
     */
    @Override
    public boolean isUseless(K8sTypes.SmartLimiter smartLimiter) {
        return smartLimiter == null ||
                smartLimiter.getSpec() == null ||
                !smartLimiter.getSpec().getSetsMap().containsKey("_base");
    }
}