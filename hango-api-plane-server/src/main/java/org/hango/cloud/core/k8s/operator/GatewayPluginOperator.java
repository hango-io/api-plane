package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import slime.microservice.plugin.v1alpha1.EnvoyPluginOuterClass;

@Component
public class GatewayPluginOperator implements k8sResourceOperator<K8sTypes.EnvoyPlugin> {

    @Override
    public K8sTypes.EnvoyPlugin merge(K8sTypes.EnvoyPlugin old, K8sTypes.EnvoyPlugin fresh) {

        K8sTypes.EnvoyPlugin latest = new K8sTypes.EnvoyPlugin();
        latest.setMetadata(fresh.getMetadata());
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        EnvoyPluginOuterClass.EnvoyPlugin.Builder builder = fresh.getSpec().toBuilder();
        latest.setSpec(builder.build());
        return latest;
    }

    @Override
    public K8sTypes.EnvoyPlugin subtract(K8sTypes.EnvoyPlugin old, String value) {
        old.setSpec(null);
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.EnvoyPlugin.name().equals(name);
    }

    @Override
    public boolean isUseless(K8sTypes.EnvoyPlugin gp) {
        return gp == null ||
                StringUtils.isEmpty(gp.getApiVersion()) ||
                 gp.getSpec() == null ||
                  CollectionUtils.isEmpty(gp.getSpec().getPluginsList());
    }
}
