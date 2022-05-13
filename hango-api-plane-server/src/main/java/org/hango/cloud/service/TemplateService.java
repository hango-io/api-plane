package org.hango.cloud.service;

import org.hango.cloud.meta.template.ServiceMeshTemplate;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public interface TemplateService {
    void updateConfig(ServiceMeshTemplate template);

    void deleteConfig(String name, String namespace, String kind);

    void deleteConfigByTemplate(String name, String namespace, String template);

    HasMetadata getConfig(String name, String namespace, String kind);

    List<HasMetadata> getConfigListByTemplate(String name, String namespace, String templateName);
}
