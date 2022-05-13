package org.hango.cloud.core.k8s.merger;

import org.hango.cloud.util.function.Merger;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.springframework.util.CollectionUtils;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class SmartLimiterMerger implements Merger<SmartLimiter> {

    @Override
    public SmartLimiter merge(SmartLimiter old, SmartLimiter latest) {

        if (isNull(latest)) {
            old.setSpec(null);
            return old;
        }
        if (isNull(old)) return latest;

        old.setSpec(latest.getSpec());
        return old;
    }

    private boolean isNull(SmartLimiter sl) {

        return sl == null ||
                sl.getSpec() == null ||
                sl.getSpec().getRatelimitConfig() == null ||
                sl.getSpec().getRatelimitConfig().getRateLimitConf() == null ||
                CollectionUtils.isEmpty(sl.getSpec().getRatelimitConfig().getRateLimitConf().getDescriptors());
    }

}
