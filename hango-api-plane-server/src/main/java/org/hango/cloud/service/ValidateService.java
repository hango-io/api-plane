package org.hango.cloud.service;

import org.hango.cloud.meta.ValidateResult;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public interface ValidateService {
    ValidateResult validate(List<HasMetadata> resources);
}
