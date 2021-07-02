package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.slime.v1alpha1.Plugin;
import me.snowdrop.istio.slime.v1alpha1.PluginManager;
import me.snowdrop.istio.slime.v1alpha1.PluginManagerBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Component
public class PluginManagerOperator implements k8sResourceOperator<PluginManager> {

    @Override
    public PluginManager merge(PluginManager old, PluginManager fresh) {

        PluginManager latest = new PluginManagerBuilder(old).build();

        List<Plugin> latestPlugins = fresh.getSpec().getPlugin();
        latest.getSpec().setPlugin(latestPlugins);
        latest.getSpec().setWorkloadLabels(fresh.getSpec().getWorkloadLabels());
        return latest;
    }

    private class PluginEquals implements Equals<Plugin> {
        @Override
        public boolean apply(Plugin op, Plugin np) {
            return Objects.equals(op.getName(), np.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.PluginManager.name().equals(name);
    }

    @Override
    public boolean isUseless(PluginManager pm) {
        return pm.getSpec() == null ||
                StringUtils.isEmpty(pm.getApiVersion()) ||
                 CollectionUtils.isEmpty(pm.getSpec().getPlugin());
    }

    @Override
    public PluginManager subtract(PluginManager old, String value) {
        old.setSpec(null);
        return old;
    }
}
