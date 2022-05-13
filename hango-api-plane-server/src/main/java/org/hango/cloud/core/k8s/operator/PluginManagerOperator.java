package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.function.Equals;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
@Component
public class PluginManagerOperator implements k8sResourceOperator<K8sTypes.PluginManager> {

    @Override
    public K8sTypes.PluginManager merge(K8sTypes.PluginManager old, K8sTypes.PluginManager fresh) {


        K8sTypes.PluginManager latest = new K8sTypes.PluginManager();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());

        PluginManagerOuterClass.PluginManager oldSpec = old.getSpec();
        PluginManagerOuterClass.PluginManager freshSpec = fresh.getSpec();
        PluginManagerOuterClass.PluginManager.Builder builder = oldSpec.toBuilder();
        if (freshSpec.getPluginCount() > 0){
            builder.clearPlugin();
            builder.addAllPlugin(freshSpec.getPluginList());
        }
        if (freshSpec.getWorkloadLabelsCount() > 0){
            builder.putAllWorkloadLabels(freshSpec.getWorkloadLabelsMap());
        }
        latest.setSpec(builder.build());
        return latest;
    }

    private class PluginEquals implements Equals<PluginManagerOuterClass.Plugin> {
        @Override
        public boolean apply(PluginManagerOuterClass.Plugin op, PluginManagerOuterClass.Plugin np) {
            return Objects.equals(op.getName(), np.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.PluginManager.name().equals(name);
    }

    @Override
    public boolean isUseless(K8sTypes.PluginManager pm) {
        return pm.getSpec() == null ||
                StringUtils.isEmpty(pm.getApiVersion()) ||
                 CollectionUtils.isEmpty(pm.getSpec().getPluginList());
    }

    @Override
    public K8sTypes.PluginManager subtract(K8sTypes.PluginManager old, String value) {
        old.setSpec(null);
        return old;
    }
}
