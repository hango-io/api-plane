package org.hango.cloud.core.k8s.operator;

import istio.networking.v1alpha3.EnvoyFilterOuterClass;
import istio.networking.v1alpha3.SidecarOuterClass;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author xin li
 * @date 2022/5/18 20:15
 */
@Component
public class EnvoyFilterOperator implements k8sResourceOperator<K8sTypes.EnvoyFilter> {
    @Override
    public K8sTypes.EnvoyFilter merge(K8sTypes.EnvoyFilter old, K8sTypes.EnvoyFilter fresh) {
        //更新配置无patch，删除最后一条协议转换
        EnvoyFilterOuterClass.EnvoyFilter freshSpec = fresh.getSpec();
        if (freshSpec.getConfigPatchesCount() == 0) {
            return fresh;
        }
        K8sTypes.EnvoyFilter latest = new K8sTypes.EnvoyFilter();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());
        EnvoyFilterOuterClass.EnvoyFilter oldSpec = old.getSpec();
        EnvoyFilterOuterClass.EnvoyFilter.Builder builder = oldSpec.toBuilder();
        if (freshSpec.getConfigPatchesCount() > 0) {
            builder.clearConfigPatches();
            builder.addAllConfigPatches(freshSpec.getConfigPatchesList());
        }
        if (freshSpec.getWorkloadSelector().getLabelsCount() > 0) {
            builder.setWorkloadSelector(SidecarOuterClass.WorkloadSelector.newBuilder().putAllLabels(freshSpec.getWorkloadSelector().getLabelsMap()).build());
        }
        latest.setSpec(builder.build());
        return latest;
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
        return envoyFilter.getSpec() == null || StringUtils.isEmpty(envoyFilter.getApiVersion()) || CollectionUtils.isEmpty(envoyFilter.getSpec().getConfigPatchesList());
    }
}
