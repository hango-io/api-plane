package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.Endpoint;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntryBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntrySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
public class ServiceEntryOperator implements k8sResourceOperator<ServiceEntry> {

    static final String GW_CLUSTER_LABEL_KEY = "gw_cluster";

    @Override
    public ServiceEntry merge(ServiceEntry old, ServiceEntry fresh) {
        ServiceEntry latest = new ServiceEntryBuilder(old).build();

        ServiceEntrySpec freshSpec = fresh.getSpec();
        ServiceEntrySpec latestSpec = latest.getSpec();
        // 直接覆盖ports
        latestSpec.setPorts(freshSpec.getPorts());
        // 合并新的和旧的endpoints
        latestSpec.setEndpoints(
                mergeList(latestSpec.getEndpoints(), freshSpec.getEndpoints(), new ServiceEntryEndpointEqual()));

        return latest;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.ServiceEntry.name().equals(name);
    }

    public static class ServiceEntryEndpointEqual implements Equals<Endpoint> {

        @Override
        public boolean apply(Endpoint oe, Endpoint ne) {
            if (CollectionUtils.isEmpty(oe.getLabels()) || CollectionUtils.isEmpty(ne.getLabels())) return false;
            return Objects.equals(oe.getLabels().get(GW_CLUSTER_LABEL_KEY), ne.getLabels().get(GW_CLUSTER_LABEL_KEY));
        }
    }

    @Override
    public boolean isUseless(ServiceEntry serviceEntry) {
        return serviceEntry == null ||
                StringUtils.isEmpty(serviceEntry.getApiVersion()) ||
                 serviceEntry.getSpec() == null;
    }

    @Override
    public ServiceEntry subtract(ServiceEntry old, String value) {
        old.setSpec(null);
        return old;
    }
}
