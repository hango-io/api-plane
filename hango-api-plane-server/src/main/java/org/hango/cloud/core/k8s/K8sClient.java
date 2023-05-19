package org.hango.cloud.core.k8s;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.hango.cloud.k8s.K8sResourceApiEnum;
import org.hango.cloud.meta.CustomResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author zhufengwei
 * @Date 2023/3/14
 */
@Component
public class K8sClient {
    private static final Logger log = LoggerFactory.getLogger(K8sClient.class);

    Map<String, CustomResourceDefinition> crdCache = new HashMap<>();

    @Autowired
    KubernetesClient client;

    @PostConstruct
    public void init(){
        //获取crd
        for (K8sResourceApiEnum value : K8sResourceApiEnum.values()) {
            CustomResourceDefinition customResourceDefinition = getCustomResourceDefinition(value.getApi());
            if (customResourceDefinition != null){
                crdCache.put(value.getApi(), customResourceDefinition);
            }
        }
    }

    public <T extends HasMetadata, L extends KubernetesResourceList<T>> List<T> getCustomResources(CustomResource<T, L> customResource, String namespace, Map<String, String> labels){
        NonNamespaceOperation<T, L, Doneable<T>, Resource<T, Doneable<T>>> operataion = getOperataion(customResource, namespace);
        return operataion.withLabels(labels).list().getItems();
    }

    public <T extends HasMetadata, L extends KubernetesResourceList<T>> T getCustomResource(CustomResource<T, L> customResource, String namespace, String name){
        NonNamespaceOperation<T, L, Doneable<T>, Resource<T, Doneable<T>>> operataion = getOperataion(customResource, namespace);
        return operataion.withName(name).get();
    }

    public <T extends HasMetadata, L extends KubernetesResourceList<T>> NonNamespaceOperation<T, L, Doneable<T>, Resource<T, Doneable<T>>> getOperataion(CustomResource<T, L> customResource, String namespace){
        CustomResourceDefinition customResourceDefinition = crdCache.get(customResource.getKind().getApi());
        if (customResourceDefinition == null){
            return null;
        }
        CustomResourceDefinitionContext customResourceDefinitionContext = CustomResourceDefinitionContext.fromCrd(customResourceDefinition);
        return client.customResources(customResourceDefinitionContext, customResource.getApiTypeClass(), customResource.getApiListTypeClass(), null).inNamespace(namespace);
    }


    private CustomResourceDefinition getCustomResourceDefinition(String name){
        try {
            return client.customResourceDefinitions().withName(name).get();
        } catch (Exception e) {
            log.error("get {} crd definition error", name, e);
            return null;
        }
    }
}
