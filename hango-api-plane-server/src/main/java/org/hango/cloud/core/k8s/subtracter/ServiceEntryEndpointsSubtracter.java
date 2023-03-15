package org.hango.cloud.core.k8s.subtracter;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.util.function.Subtracter;
import me.snowdrop.istio.api.networking.v1alpha3.Endpoint;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServiceEntryEndpointsSubtracter implements Subtracter<ServiceEntry> {

    private String gateway;

    public ServiceEntryEndpointsSubtracter(String gateway) {
        this.gateway = gateway;
    }

    @Override
    public ServiceEntry subtract(ServiceEntry serviceEntry) {

        if (serviceEntry == null || serviceEntry.getSpec() == null ||
                serviceEntry.getSpec().getEndpoints() == null) return serviceEntry;

        Map<String, String> targetLabel = ImmutableMap.of("gw_cluster", gateway);

        List<Endpoint> filteredEndpoints = serviceEntry.getSpec().getEndpoints().stream()
                .filter(e -> !Objects.equals(e.getLabels(), targetLabel))
                .collect(Collectors.toList());

        serviceEntry.getSpec().setEndpoints(filteredEndpoints);
        return serviceEntry;
    }
}
