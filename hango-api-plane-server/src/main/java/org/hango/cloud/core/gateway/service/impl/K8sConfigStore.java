package org.hango.cloud.core.gateway.service.impl;

import org.hango.cloud.core.ConfigStore;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.core.template.TemplateConst;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用k8s作为存储
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
public class K8sConfigStore implements ConfigStore {

    KubernetesClient client;
    GlobalConfig globalConfig;

    public K8sConfigStore(KubernetesClient client, GlobalConfig globalConfig) {
        this.client = client;
        this.globalConfig = globalConfig;
    }

    @Override
    public void delete(HasMetadata resource) {
        supply(resource);
        HasMetadata r = resource;
        ObjectMeta meta = r.getMetadata();
        client.delete(r.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public void update(HasMetadata resource) {
        supply(resource);
        client.createOrUpdate(resource, ResourceType.OBJECT);
    }

    @Override
    public HasMetadata get(HasMetadata resource) {
        supply(resource);
        HasMetadata r = resource;
        ObjectMeta meta = r.getMetadata();
        return get(r.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public HasMetadata get(String kind, String namespace, String name) {
        if (StringUtils.isEmpty(namespace)) {
            namespace = globalConfig.getResourceNamespace();
        }
        return client.getObject(kind, namespace, name);
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            namespace = globalConfig.getResourceNamespace();
        }
        return client.getObjectList(kind, namespace);
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace, Map<String, String> labels) {
        if (StringUtils.isEmpty(namespace)) {
            namespace = globalConfig.getResourceNamespace();
        }
        return client.getObjectList(kind, namespace, labels);
    }

    protected void supply(HasMetadata resource) {
        if (isGlobalCrd(resource)) return;

        ObjectMeta metadata = resource.getMetadata();
        if (metadata != null) {
            if (StringUtils.isEmpty(metadata.getNamespace())) {
                metadata.setNamespace(globalConfig.getResourceNamespace());
            }
            HashMap<String, String> oldLabels = metadata.getLabels() == null ?
                    new HashMap<>() : new HashMap<>(metadata.getLabels());
            oldLabels.put(TemplateConst.LABEL_API_PLANE_TYPE, globalConfig.getApiPlaneType());
            oldLabels.put(TemplateConst.LABEL_API_PLANE_VERSION, globalConfig.getApiPlaneVersion());
            oldLabels.put(TemplateConst.LABLE_ISTIO_REV, globalConfig.getIstioRev());
            metadata.setLabels(oldLabels);
        }
    }

    boolean isGlobalCrd(HasMetadata resource) {
        return K8sResourceEnum.get(resource.getKind()).isClustered();
    }
}
