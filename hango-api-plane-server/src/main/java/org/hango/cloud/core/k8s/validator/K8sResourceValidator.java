package org.hango.cloud.core.k8s.validator;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Set;

public interface K8sResourceValidator<T extends HasMetadata> {
    boolean adapt(String name);

    Set<ConstraintViolation<T>> validate(T var);
}
