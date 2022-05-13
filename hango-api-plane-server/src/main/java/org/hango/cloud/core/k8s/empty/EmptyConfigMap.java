package org.hango.cloud.core.k8s.empty;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/19
 **/
public class EmptyConfigMap extends ConfigMap implements HasMetadata, EmptyResource {

    private ObjectMeta om;

    public EmptyConfigMap(String name) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        this.om = tom;
    }

    public EmptyConfigMap(String name, String namespace) {
        ObjectMeta om = new ObjectMeta();
        om.setName(name);
        om.setNamespace(namespace);
        this.om = om;
    }

    @Override
    public ObjectMeta getMetadata() {
        return om;
    }




}
