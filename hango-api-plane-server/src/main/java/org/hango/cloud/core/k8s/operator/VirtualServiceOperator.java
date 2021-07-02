package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class VirtualServiceOperator implements k8sResourceOperator<VirtualService> {

    @Override
    public VirtualService merge(VirtualService old, VirtualService fresh) {

        VirtualServiceSpec oldSpec = old.getSpec();
        VirtualServiceSpec freshSpec = fresh.getSpec();

        VirtualService latest = new VirtualServiceBuilder(old).build();

        List<HTTPRoute> latestHttp = mergeList(oldSpec.getHttp(), freshSpec.getHttp(), new HttpRouteEquals());
        latest.getSpec().setHttp(latestHttp);

        latest.getSpec().setHosts(fresh.getSpec().getHosts());

        latest.getSpec().setPriority(fresh.getSpec().getPriority());

        //VirtualCluster 合并
        latest.getSpec().setVirtualCluster(fresh.getSpec().getVirtualCluster());
        return latest;
    }

    private class HttpRouteEquals implements Equals<HTTPRoute> {
        @Override
        public boolean apply(HTTPRoute ot, HTTPRoute nt) {
            return Objects.equals(ot.getName(), nt.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VirtualService.name().equals(name);
    }

    @Override
    public boolean isUseless(VirtualService virtualService) {
        return virtualService == null ||
                StringUtils.isEmpty(virtualService.getApiVersion()) ||
                 virtualService.getSpec() == null ||
                  CollectionUtils.isEmpty(virtualService.getSpec().getHttp());
    }

    @Override
    public VirtualService subtract(VirtualService old, String value) {

        //根据api name删除httpRoute
        List<HTTPRoute> latestHttp = old.getSpec().getHttp().stream()
                .filter(h -> !h.getName().equals(value))
                .collect(Collectors.toList());

        old.getSpec().setHttp(latestHttp);
        return old;
    }
}
