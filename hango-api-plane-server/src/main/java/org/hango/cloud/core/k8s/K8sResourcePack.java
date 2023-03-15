package org.hango.cloud.core.k8s;

import org.hango.cloud.util.function.Merger;
import org.hango.cloud.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * k8s资源 + 操作工具
 */
public class K8sResourcePack {

    private HasMetadata resource;

    private Merger<HasMetadata> merger;

    private Subtracter<HasMetadata> subtracter;

    public K8sResourcePack(HasMetadata resource) {
        this.resource = resource;
    }

    public K8sResourcePack(HasMetadata resource, Merger merger, Subtracter subtracter) {
        this.resource = resource;
        this.merger = merger;
        this.subtracter = subtracter;
    }

    public boolean hasMerger() {
        return this.merger != null;
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

    public Merger<HasMetadata> getMerger() {
        return merger;
    }

    public void setMerger(Merger<HasMetadata> merger) {
        this.merger = merger;
    }

    public Subtracter<HasMetadata> getSubtracter() {
        return subtracter;
    }

    public void setSubtracter(Subtracter<HasMetadata> subtracter) {
        this.subtracter = subtracter;
    }
}
