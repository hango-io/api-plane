package org.hango.cloud.core.k8s;

import org.hango.cloud.core.k8s.event.NotifyRateLimitServerEvent;
import org.hango.cloud.core.k8s.event.RlsInfo;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/19
 **/
public class MeshK8sEventWatcher {

    private static final Logger logger = LoggerFactory.getLogger(MeshK8sEventWatcher.class);

    private MultiClusterK8sClient multiClient;

    @Autowired
    public MeshK8sEventWatcher(MultiClusterK8sClient multiClient) {
        this.multiClient = multiClient;
    }

    /**
     * 为特定的pod打上annotation
     * @param event
     */
    @Async
    @EventListener
    public void annotatePodWithLabels(NotifyRateLimitServerEvent event) {

        logger.info("receive NotifyRateLimitServerEvent, {}", event);

        RlsInfo rlsInfo = (RlsInfo) event.getSource();
        Map<String, String> labels = rlsInfo.getLabels();
        String clusterId = rlsInfo.getClusterId();
        String namespace = rlsInfo.getNamespace();
        String key = rlsInfo.getKey();
        String val = rlsInfo.getVal();
        if (CollectionUtils.isEmpty(labels) || StringUtils.isEmpty(clusterId)
                || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(key) || StringUtils.isEmpty(val)) return;
        io.fabric8.kubernetes.client.KubernetesClient client = multiClient.originalK8sClient(clusterId);
        PodList podList = client.pods().inNamespace(namespace).withLabels(labels).list();
        if (podList != null && !CollectionUtils.isEmpty(podList.getItems())) {
            for (Pod pod : podList.getItems()) {
                String podName = pod.getMetadata().getName();
                Pod patch = buildPodPatch(pod, key, val);
                if (Objects.equals(pod.getStatus().getPhase(), "Running")) {
                    client.pods().inNamespace(namespace).withName(podName).patch(patch);
                }
            }
        }
    }

    private Pod buildPodPatch(Pod pod, String key, String value) {
        Map<String, String> annotations = pod.getMetadata().getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<>();
        }
        annotations.put(key, value);
        pod.getMetadata().setAnnotations(annotations);
        return pod;
    }
}
