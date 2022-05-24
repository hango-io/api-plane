package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.function.Subtracter;

public class GatewayPluginNormalSubtracter implements Subtracter<K8sTypes.EnvoyPlugin> {
    @Override
    public K8sTypes.EnvoyPlugin subtract(K8sTypes.EnvoyPlugin gatewayPlugin) {
        gatewayPlugin.setSpec(null);
        return gatewayPlugin;
    }
}
