package org.hango.cloud.core.k8s.operator;

import me.snowdrop.istio.slime.v1alpha1.EnvoyPlugin;
import me.snowdrop.istio.slime.v1alpha1.EnvoyPluginBuilder;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import me.snowdrop.istio.slime.v1alpha1.Plugin;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class GatewayPluginOperator implements k8sResourceOperator<EnvoyPlugin> {

    @Override
    public EnvoyPlugin merge(EnvoyPlugin old, EnvoyPlugin fresh) {

        EnvoyPlugin latest = new EnvoyPluginBuilder(old).build();

        List<Plugin> latestPlugins = fresh.getSpec().getPlugins();
        latest.getSpec().setPlugins(latestPlugins);
        latest.getSpec().setHost(fresh.getSpec().getHost());
        latest.getSpec().setGateway(fresh.getSpec().getGateway());
        latest.getSpec().setRoute(fresh.getSpec().getRoute());
        latest.getSpec().setService(fresh.getSpec().getService());
        if (fresh.getMetadata() != null && fresh.getMetadata().getLabels() != null) {
            latest.getMetadata().setLabels(fresh.getMetadata().getLabels());
        }
        return latest;
    }

    @Override
    public EnvoyPlugin subtract(EnvoyPlugin old, String value) {
        old.setSpec(null);
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.EnvoyPlugin.name().equals(name);
    }

    @Override
    public boolean isUseless(EnvoyPlugin gp) {
        return gp == null ||
                StringUtils.isEmpty(gp.getApiVersion()) ||
                 gp.getSpec() == null ||
                  CollectionUtils.isEmpty(gp.getSpec().getPlugins());
    }
}
