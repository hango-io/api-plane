package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.function.Subtracter;
import istio.networking.v1alpha3.VirtualServiceOuterClass;

import java.util.List;
import java.util.stream.Collectors;

public class GatewayVirtualServiceSubtracter implements Subtracter<K8sTypes.VirtualService> {

    private String key;

    public GatewayVirtualServiceSubtracter(String key) {
        this.key = key;
    }

    @Override
    public K8sTypes.VirtualService subtract(K8sTypes.VirtualService old) {
        List<VirtualServiceOuterClass.HTTPRoute> latestHttp = old.getSpec().getHttpList().stream()
                .filter(h -> !h.getName().equals(key))
                .collect(Collectors.toList());

        VirtualServiceOuterClass.VirtualService build = old.getSpec().toBuilder()
                .clearHttp()
                .clearTcp()
                .clearUdp()
                .build();
        //TCP/UDP 默认路由不存在更新场景
        old.setSpec(build.toBuilder().addAllHttp(latestHttp).build());
        return old;
    }
}
