package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.configuration.ext.MeshConfig;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.GlobalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GlobalConfigOperator implements k8sResourceOperator<GlobalConfig> {
    @Autowired
    private MeshConfig meshConfig;

    @Override
    public GlobalConfig merge(GlobalConfig old, GlobalConfig fresh) {
        //do nothing
        return fresh;
    }

    @Override
    public GlobalConfig subtract(GlobalConfig old, String value) {
        //do nothing
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.GlobalConfig.name().equals(name);
    }

    @Override
    public boolean isUseless(GlobalConfig globalConfig) {
        return globalConfig == null ||
                StringUtils.isEmpty(globalConfig.getApiVersion()) ||
                globalConfig.getSpec() == null ||
                globalConfig.getSpec().getBody() == null;
    }
}
