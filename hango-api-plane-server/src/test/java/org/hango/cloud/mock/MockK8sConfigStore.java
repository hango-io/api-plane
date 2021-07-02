package org.hango.cloud.mock;

import org.hango.cloud.core.ConfigStore;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.*;

public class MockK8sConfigStore implements ConfigStore {

    private Map<ResourceId, HasMetadata> store = new HashMap<>();

    @Override
    public void delete(HasMetadata resource) {
        store.remove(getId(resource));
    }

    @Override
    public void update(HasMetadata resource) {
        store.put(getId(resource), resource);
    }

    @Override
    public HasMetadata get(HasMetadata resource) {
        return store.get(getId(resource));
    }

    @Override
    public HasMetadata get(String kind, String namespace, String name) {
        return store.get(getId(kind, name, namespace));
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace, Map<String, String> labels) {
        List<HasMetadata> resources = new ArrayList<>();
        for (HasMetadata res : store.values()) {
            if (kind.equals(res.getKind()) &&
                    namespace.equals(res.getMetadata().getNamespace()) &&
                    res.getMetadata().getLabels().entrySet().containsAll(labels.entrySet())) {
                resources.add(res);
            }
        }
        return resources;
    }

    public Map<ResourceId, HasMetadata> map() {
        return store;
    }

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }

    private ResourceId getId(HasMetadata hasMetadata) {
        return new ResourceId(hasMetadata.getKind(), hasMetadata.getMetadata().getName(), hasMetadata.getMetadata().getNamespace());
    }

    private ResourceId getId(String kind, String name, String namespace) {
        return new ResourceId(kind, name, namespace);
    }

    public static class ResourceId {
        private String kind;
        private String name;
        private String namespace;

        public ResourceId(String kind, String name, String namespace) {
            this.kind = kind;
            this.name = name;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourceId that = (ResourceId) o;
            return Objects.equals(kind, that.kind) &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(namespace, that.namespace);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, name, namespace);
        }
    }

}
