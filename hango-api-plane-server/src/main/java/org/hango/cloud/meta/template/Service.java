package org.hango.cloud.meta.template;

import org.hibernate.validator.constraints.NotEmpty;

public class Service {

    @NotEmpty(message = "name")
    private String name;

    @NotEmpty(message = "namespace")
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
}
