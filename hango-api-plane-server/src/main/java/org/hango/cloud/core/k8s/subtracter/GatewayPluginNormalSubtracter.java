package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.util.function.Subtracter;
import me.snowdrop.istio.slime.v1alpha1.EnvoyPlugin;

public class GatewayPluginNormalSubtracter implements Subtracter<EnvoyPlugin> {
    @Override
    public EnvoyPlugin subtract(EnvoyPlugin gatewayPlugin) {
        gatewayPlugin.setSpec(null);
        return gatewayPlugin;
    }
}
