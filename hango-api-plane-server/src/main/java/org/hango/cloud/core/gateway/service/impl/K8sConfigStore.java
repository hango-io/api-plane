package org.hango.cloud.core.gateway.service.impl;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.hango.cloud.core.ConfigStore;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.core.template.TemplateConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用k8s作为存储
 **/
public class K8sConfigStore implements ConfigStore {

    private static final Logger logger = LoggerFactory.getLogger(K8sConfigStore.class);

    KubernetesClient client;
    List<KubernetesClient> nodeClients;
    GlobalConfig globalConfig;
    //多集群节点线程池
    private ExecutorService executorService;

    public K8sConfigStore(MultiClusterK8sClient multiClusterK8sClient, GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        if (multiClusterK8sClient != null){
            this.nodeClients = multiClusterK8sClient.getNodeClients();
            this.client = multiClusterK8sClient.getMasterClient();
            //存在其他集群，则初始化线程池进行配置下发
            if (nodeClients.size() > 0){
                executorService = Executors.newFixedThreadPool(nodeClients.size());
            }
        }
    }

    @Override
    public void delete(HasMetadata resource) {
        supply(resource);
        ObjectMeta meta = resource.getMetadata();
        client.delete(resource.getKind(), meta.getNamespace(), meta.getName());
        //多集群需要异步删除其他集群中的配置
        if (!CollectionUtils.isEmpty(nodeClients)){
            for (KubernetesClient client : nodeClients) {
                executorService.execute(() -> {
                    logger.info("{}|start handle cluster delete task", client.getClientName());
                    try {
                        client.delete(resource.getKind(), meta.getNamespace(), meta.getName());
                    } catch (Exception exception) {
                        logger.error("{}|handle task execption", client.getClientName(), exception);
                    }
                    logger.info("{}|finish handle cluster delete task", client.getClientName());
                });
            }
        }
    }

    @Override
    public void update(HasMetadata resource) {
        supply(resource);
        client.createOrUpdate(resource, ResourceType.OBJECT);
        //多集群需要异步更新其他集群中的配置
        if (!CollectionUtils.isEmpty(nodeClients)){
            for (KubernetesClient client : nodeClients) {
                executorService.execute(() -> {
                    logger.info("{}|start handle cluster update task", client.getClientName());
                    try {
                        client.createOrUpdate(resource, ResourceType.OBJECT);
                    } catch (Exception exception) {
                        logger.error("{}|handle task execption", client.getClientName(), exception);
                    }
                    logger.info("{}|finish handle cluster update task", client.getClientName());
                });
            }
        }
    }

    @Override
    public HasMetadata get(HasMetadata resource) {
        supply(resource);
        ObjectMeta meta = resource.getMetadata();
        return get(resource.getKind(), meta.getNamespace(), meta.getName());
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
            oldLabels.put(TemplateConst.LABLE_ISTIO_REV, globalConfig.getIstioRev());
            metadata.setLabels(oldLabels);
        }
    }

    boolean isGlobalCrd(HasMetadata resource) {
        return K8sResourceEnum.get(resource.getKind()).isClustered();
    }
}
