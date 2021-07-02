package org.hango.cloud.core.k8s.validator;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class IntegratedValidator<T extends HasMetadata> implements K8sResourceValidator<T> {

    private List<K8sResourceValidator> validators;

    @Autowired
    public IntegratedValidator(List<K8sResourceValidator> validators) {
        this.validators = validators;
    }

    @Override
    public boolean adapt(String name) {
        return true;
    }

    @Override
    public Set<ConstraintViolation<T>> validate(T var) {
        Set<ConstraintViolation<T>> violations = new LinkedHashSet<>();
        validators.forEach(validator -> {
            if (validator.adapt(var.getKind())) {
                violations.addAll(validator.validate(var));
            }
        });
        return violations;
    }
}
