package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.function.Equals;
import istio.networking.v1alpha3.VirtualServiceOuterClass;
import istio.networking.v1alpha3.VirtualServiceOuterClass.VirtualService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
@Component
public class VirtualServiceOperator implements k8sResourceOperator<K8sTypes.VirtualService> {

    @Override
    public K8sTypes.VirtualService merge(K8sTypes.VirtualService old, K8sTypes.VirtualService fresh) {

        VirtualService oldSpec = old.getSpec();
        VirtualService freshSpec = fresh.getSpec();

        /**
         * 深拷贝，后续改造构造函数，是在clone方法。nsf-proto
         */
        K8sTypes.VirtualService latest = new K8sTypes.VirtualService();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());
        latest.setSpec(oldSpec);

        List<VirtualServiceOuterClass.HTTPRoute> latestHttp = mergeList(oldSpec.getHttpList(), freshSpec.getHttpList(), new HttpRouteEquals());

        latest.setSpec(VirtualService.newBuilder()
                .addAllHttp(latestHttp)
                .addAllVirtualCluster(freshSpec.getVirtualClusterList())
                .addAllHosts(freshSpec.getHostsList())
                .addAllGateways(freshSpec.getGatewaysList())
                .setPriority(freshSpec.getPriority()).build());
        return latest;
    }

    private class HttpRouteEquals implements Equals<VirtualServiceOuterClass.HTTPRoute> {
        @Override
        public boolean apply(VirtualServiceOuterClass.HTTPRoute ot, VirtualServiceOuterClass.HTTPRoute nt) {
            return Objects.equals(ot.getName(), nt.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VirtualService.name().equals(name);
    }

    @Override
    public boolean isUseless(K8sTypes.VirtualService virtualService) {
        return virtualService == null ||
                StringUtils.isEmpty(virtualService.getApiVersion()) ||
                virtualService.getSpec() == null ||
                CollectionUtils.isEmpty(virtualService.getSpec().getHttpList());
    }

    @Override
    public K8sTypes.VirtualService subtract(K8sTypes.VirtualService old, String value) {

        //根据name删除httpRoute
        List<VirtualServiceOuterClass.HTTPRoute> latestHttp = old.getSpec().getHttpList().stream()
                .filter(h -> !h.getName().equals(value))
                .collect(Collectors.toList());

        VirtualService build = old.getSpec().toBuilder().clearHttp().build();
        old.setSpec(build.toBuilder().addAllHttp(latestHttp).build());
        return old;
    }
}