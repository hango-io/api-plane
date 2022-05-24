package org.hango.cloud.core.k8s.empty;

import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class EmptySmartLimiter extends SmartLimiter implements HasMetadata, EmptyResource {

    private ObjectMeta om;

    public EmptySmartLimiter(String name, String namespace) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        tom.setNamespace(namespace);
        this.om = tom;
    }

    @Override
    public ObjectMeta getMetadata() {
        return om;
    }
}
