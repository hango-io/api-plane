package org.hango.cloud.configuration.ext;

import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.springframework.context.annotation.Bean;

public class IstioSupportConfiguration {

    @Bean
    public KubernetesClient kubernetesClient(MultiClusterK8sClient mc) {
    	return mc.k8sClient(MultiClusterK8sClient.DEFAULT_CLUSTER_NAME);
    }

    @Bean("originalKubernetesClient")
    public io.fabric8.kubernetes.client.KubernetesClient originalKubernetesClient(MultiClusterK8sClient mc) {
        return mc.originalK8sClient(MultiClusterK8sClient.DEFAULT_CLUSTER_NAME);
    }

    @Bean
    public MultiClusterK8sClient multiClusterK8sClient(K8sMultiClusterProperties properties, EditorContext editorContext) {
        return new MultiClusterK8sClient(properties, editorContext);
    }
}

