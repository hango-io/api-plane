package org.hango.cloud.meta.template;

import javax.validation.constraints.NotNull;


public class Metadata {

    @NotNull(message = "metadata name")
    private String name;

    @NotNull(message = "metadata namespace")
    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    public static final class MetadataBuilder {
        private String name;
        private String namespace;

        private MetadataBuilder() {
        }

        public static MetadataBuilder aMetadata() {
            return new MetadataBuilder();
        }

        public MetadataBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MetadataBuilder withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Metadata build() {
            Metadata metadata = new Metadata();
            metadata.setName(name);
            metadata.setNamespace(namespace);
            return metadata;
        }
    }
}
