package org.hango.cloud.core.k8s.empty;

import org.hango.cloud.k8s.K8sTypes;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/19
 **/
public class EmptyGatewayPlugin extends K8sTypes.EnvoyPlugin implements HasMetadata, EmptyResource{

    private ObjectMeta om;

    public EmptyGatewayPlugin(String name, String namespace) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        tom.setNamespace(namespace);
        this.om = tom;
    }

    public EmptyGatewayPlugin(String name) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        this.om = tom;
    }


    @Override
    public ObjectMeta getMetadata() {
        return om;
    }

}
