package org.hango.cloud.core.k8s;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.hango.cloud.util.function.Subtracter;

/**
 * k8s资源 + 操作工具
 */
public class K8sResourcePack {

    private HasMetadata resource;


    private Subtracter<HasMetadata> subtracter;

    public K8sResourcePack(HasMetadata resource) {
        this.resource = resource;
    }

    public K8sResourcePack(HasMetadata resource, Subtracter subtracter) {
        this.resource = resource;
        this.subtracter = subtracter;
    }

    public boolean hasSubtracter() {
        return this.subtracter != null;
    }

    public HasMetadata getResource() {
        return resource;
    }

    public void setResource(HasMetadata resource) {
        this.resource = resource;
    }


    public Subtracter<HasMetadata> getSubtracter() {
        return subtracter;
    }

    public void setSubtracter(Subtracter<HasMetadata> subtracter) {
        this.subtracter = subtracter;
    }
}
