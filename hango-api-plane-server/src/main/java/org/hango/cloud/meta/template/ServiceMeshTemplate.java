package org.hango.cloud.meta.template;

import org.hango.cloud.util.validator.annotation.ConditionalTemplate;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@ConditionalTemplate(templateName = "whiteList", required = {"outWeight", "targetList"})
@ConditionalTemplate(templateName = "cloudShuttle", required = {"outWeight", "targetList"})
@ConditionalTemplate(templateName = "faultInject", required = {"host", "percent", "fixedDelay", "httpStatus", "destinations"})
@ConditionalTemplate(templateName = "loadBalancing", required = {"host", "simple"})
public class ServiceMeshTemplate {

    @NotEmpty(message = "template")
    private String nsfTemplate;

    @Valid
    @NotNull(message = "nsfExtra")
    private NsfExtra nsfExtra;

    @Valid
    @NotNull(message = "metadata")
    private Metadata metadata;

    private Boolean update;

    public String getNsfTemplate() {
        return nsfTemplate;
    }

    public void setNsfTemplate(String nsfTemplate) {
        this.nsfTemplate = nsfTemplate;
    }

    public NsfExtra getNsfExtra() {
        return nsfExtra;
    }

    public void setNsfExtra(NsfExtra nsfExtra) {
        this.nsfExtra = nsfExtra;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public static final class ServiceMeshTemplateBuilder {
        private String nsfTemplate;
        private NsfExtra nsfExtra;
        private Metadata metadata;

        private ServiceMeshTemplateBuilder() {
        }

        public static ServiceMeshTemplateBuilder aServiceMeshTemplate() {
            return new ServiceMeshTemplateBuilder();
        }

        public ServiceMeshTemplateBuilder withNsfTemplate(String nsfTemplate) {
            this.nsfTemplate = nsfTemplate;
            return this;
        }

        public ServiceMeshTemplateBuilder withNsfExtra(NsfExtra nsfExtra) {
            this.nsfExtra = nsfExtra;
            return this;
        }

        public ServiceMeshTemplateBuilder withMetadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServiceMeshTemplate build() {
            ServiceMeshTemplate serviceMeshTemplate = new ServiceMeshTemplate();
            serviceMeshTemplate.setNsfTemplate(nsfTemplate);
            serviceMeshTemplate.setNsfExtra(nsfExtra);
            serviceMeshTemplate.setMetadata(metadata);
            return serviceMeshTemplate;
        }
    }
}
