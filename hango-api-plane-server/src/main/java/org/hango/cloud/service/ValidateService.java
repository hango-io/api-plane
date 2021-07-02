package org.hango.cloud.service;

import org.hango.cloud.meta.ValidateResult;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

public interface ValidateService {
    ValidateResult validate(List<HasMetadata> resources);
}
