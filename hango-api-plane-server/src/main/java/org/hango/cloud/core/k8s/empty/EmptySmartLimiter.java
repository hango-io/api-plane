package org.hango.cloud.core.k8s.empty;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.hango.cloud.k8s.K8sTypes;

public class EmptySmartLimiter extends K8sTypes.SmartLimiter implements HasMetadata, EmptyResource {

    private ObjectMeta om;

    public EmptySmartLimiter(String name, String namespace) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        tom.setNamespace(namespace);
        this.om = tom;
    }

    public EmptySmartLimiter(String name) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        this.om = tom;
    }

    @Override
    public ObjectMeta getMetadata() {
        return om;
    }
}
