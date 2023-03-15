package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.Sidecar;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SidecarOperator implements k8sResourceOperator<Sidecar> {

    /**
     *  增量增加hosts
     * @param old
     * @param fresh
     * @return
     */
    @Override
    public Sidecar merge(Sidecar old, Sidecar fresh) {

        if (isUseless(fresh)) return old;
        if (isUseless(old)) return fresh;

        List<String> freshHosts = fresh.getSpec().getEgress().get(0).getHosts();
        List<String> oldHosts = old.getSpec().getEgress().get(0).getHosts();

        oldHosts.addAll(freshHosts);
        List<String> finalHosts = oldHosts.stream().distinct().collect(Collectors.toList());
        old.getSpec().getEgress().get(0).setHosts(finalHosts);
        return old;
    }


    @Override
    public Sidecar subtract(Sidecar old, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.Sidecar.name().equals(name);
    }

    @Override
    public boolean isUseless(Sidecar sidecar) {
        return sidecar == null ||
                sidecar.getSpec() == null ||
                CollectionUtils.isEmpty(sidecar.getSpec().getEgress()) ||
                sidecar.getSpec().getEgress().get(0) == null ||
                CollectionUtils.isEmpty(sidecar.getSpec().getEgress().get(0).getHosts());
    }
}
