package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.util.function.Subtracter;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;

import java.util.List;
import java.util.stream.Collectors;

public class GatewayVirtualServiceSubtracter implements Subtracter<VirtualService> {

    private String key;

    public GatewayVirtualServiceSubtracter(String key) {
        this.key = key;
    }

    @Override
    public VirtualService subtract(VirtualService old) {
        List<HTTPRoute> latestHttp = old.getSpec().getHttp().stream()
                .filter(h -> !h.getName().equals(key))
                .collect(Collectors.toList());

        old.getSpec().setHttp(latestHttp);
        return old;
    }
}
