package org.hango.cloud.core.k8s.empty;

import org.hango.cloud.util.exception.ApiPlaneException;
import me.snowdrop.istio.slime.v1alpha1.EnvoyPlugin;

import java.util.List;
import java.util.function.Supplier;

public class DynamicGatewayPluginSupplier implements Supplier<EnvoyPlugin> {

    private int index;
    private int limit;
    private List<String> gws;
    private String name;
    private String format;

    public DynamicGatewayPluginSupplier(List<String> gws, String name, String format) {
        this.gws = gws;
        this.name = name;
        this.format = format;
        this.limit = gws.size() - 1;
        this.index = 0;
    }

    @Override
    public EnvoyPlugin get() {
        if (index > limit) throw new ApiPlaneException("out of limit, gateway plugin supplier");
        String realName = String.format(format, name, gws.get(index++));
        return new EmptyGatewayPlugin(realName);
    }
}
