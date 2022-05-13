package org.hango.cloud.service.impl;

import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.core.template.TemplateTranslator;
import org.hango.cloud.meta.template.Metadata;
import org.hango.cloud.meta.template.NsfExtra;
import org.hango.cloud.meta.template.ServiceMeshTemplate;
import org.hango.cloud.service.TemplateService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Service
public class TemplateServiceImpl implements TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);


    @Autowired
    private TemplateTranslator translator;

    @Autowired
    private EditorContext editorContext;

    @Autowired
    private KubernetesClient client;

    private static final String YAML_SPLIT = "---";

    public void updateConfig(ServiceMeshTemplate template) {
        template.setUpdate(true);
        String content = translator.translate(template.getNsfTemplate(), template);
        logger.info("update config : \r" + content);
        splitContent(content).forEach(o -> client.createOrUpdate(o, ResourceType.YAML));
    }

    public List<String> splitContent(String content) {
        List<String> contents = new ArrayList<>();
        for (String segment : content.split(YAML_SPLIT)) {
            if (segment.contains("apiVersion")) {
                contents.add(segment);
            }
        }
        return contents;
    }


    public void deleteConfig(String name, String namespace, String kind) {
        logger.info("delete config by name: {}, namespace: {}, kind: {}", name, namespace, kind);
        client.delete(kind, namespace, name);
    }

    public void deleteConfigByTemplate(String name, String namespace, String templateName) {
        logger.info("delete config by name: {}, namespace: {}, templateName: {}", name, namespace, templateName);

        // 1. 渲染模板
        ServiceMeshTemplate template = blankTemplate(name, namespace, templateName);
        String content = translator.translate(template.getNsfTemplate(), templateName);

        // 2. 分割模板 + 删除
        splitContent(content).forEach(o -> {
            K8sResourceGenerator gen = K8sResourceGenerator.newInstance(o, ResourceType.YAML);
            deleteConfig(gen.getName(), gen.getNamespace(), gen.getKind());
        });
    }

    public HasMetadata getConfig(String name, String namespace, String kind) {
        logger.info("get config by name: {}, namespace: {}, kind: {}", name, namespace, kind);
        return client.getObject(kind, namespace, name);
    }


    public List<HasMetadata> getConfigListByTemplate(String name, String namespace, String templateName) {
        List<HasMetadata> remoteResources = new ArrayList<>();

        // 1. 生成空模板
        ServiceMeshTemplate template = blankTemplate(name, namespace, templateName);

        // 2. 渲染模板
        String content = translator.translate(template.getNsfTemplate(), template);

        // 3. 分割模板 + 查询
        splitContent(content).forEach(o -> {
            K8sResourceGenerator gen = K8sResourceGenerator.newInstance(o, ResourceType.YAML);
            remoteResources.add(getConfig(gen.getName(), gen.getNamespace(), gen.getKind()));
        });
        return remoteResources;
    }

    /**
     * 生成一个空模板，用于删除或查询操作，
     * 主要为填充模板的占位符，才能顺利解析，然后得到模板的kind
     *
     * @param name
     * @param namespace
     * @param templateName
     * @return
     */
    private ServiceMeshTemplate blankTemplate(String name, String namespace, String templateName) {
        return ServiceMeshTemplate.ServiceMeshTemplateBuilder.aServiceMeshTemplate()
                .withMetadata(Metadata.MetadataBuilder.aMetadata()
                        .withName(name)
                        .withNamespace(namespace)
                        .build())
                .withNsfExtra(NsfExtra.NsfExtraBuilder.aNsfExtra()
                        .build())
                .withNsfTemplate(templateName)
                .build();
    }

}
