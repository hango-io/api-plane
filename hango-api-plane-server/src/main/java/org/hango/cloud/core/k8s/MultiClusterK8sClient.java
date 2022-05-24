package org.hango.cloud.core.k8s;

import org.hango.cloud.configuration.ext.K8sMultiClusterProperties;
import org.hango.cloud.core.editor.EditorContext;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static okhttp3.TlsVersion.TLS_1_1;
import static okhttp3.TlsVersion.TLS_1_2;

public class MultiClusterK8sClient {
	public static final String DEFAULT_CLUSTER_NAME = "default";
	private final Map<String, ClientSet> clients = new ConcurrentHashMap<>();

	public MultiClusterK8sClient(K8sMultiClusterProperties properties, EditorContext editorContext) {
		Map<String, K8sMultiClusterProperties.K8sClusterConfig> clusters = properties.getClusters();
		if (clusters == null || !clusters.containsKey(DEFAULT_CLUSTER_NAME)) {
			throw new IllegalStateException("Must have an k8s cluster config as key: '" + DEFAULT_CLUSTER_NAME + "'.");
		}
		for (Map.Entry<String, K8sMultiClusterProperties.K8sClusterConfig> entry : clusters.entrySet()) {
			Config config = getConfig(entry.getValue());
			OkHttpClient httpClient = HttpClientUtils.createHttpClient(config);
			KubernetesClient k8sClient = new KubernetesClient(config, httpClient, editorContext);
			io.fabric8.kubernetes.client.KubernetesClient originalK8sClient = new DefaultKubernetesClient(httpClient, config);
			clients.put(entry.getKey(), new ClientSet(k8sClient, originalK8sClient, entry.getValue().isWatchResource()));
		}
	}

	private static Config getConfig(K8sMultiClusterProperties.K8sClusterConfig clusterConfig) {
		return StringUtils.isEmpty(clusterConfig.getK8sApiServer()) ? Config.autoConfigure(null) :
			new ConfigBuilder()
				.withMasterUrl(clusterConfig.getK8sApiServer())
				.withTrustCerts(true)
				.withDisableHostnameVerification(true)
				.withClientCertData(clusterConfig.getCertData())
				.withClientKeyData(clusterConfig.getKeyData())
				.withClientKeyPassphrase("passphrase")
				.withWatchReconnectInterval(5000)
				.withWatchReconnectLimit(5)
				.withRequestTimeout(5000)
				.withTlsVersions(TLS_1_2, TLS_1_1)
				.build();
	}

	public Map<String, ClientSet> getAllClients() {
		return clients;
	}

	public KubernetesClient k8sClient(String clusterName) {
		return clients.getOrDefault(clusterName, ClientSet.fakeClient).k8sClient;
	}

	public io.fabric8.kubernetes.client.KubernetesClient originalK8sClient(String clusterName) {
		return clients.getOrDefault(clusterName, ClientSet.fakeClient).originalK8sClient;
	}

	public static class ClientSet {
		public final KubernetesClient k8sClient;
		public final io.fabric8.kubernetes.client.KubernetesClient originalK8sClient;
		public final boolean watchResource;

		private static final ClientSet fakeClient = new ClientSet(null, null, false);
		private ClientSet(KubernetesClient k8sClient, io.fabric8.kubernetes.client.KubernetesClient originalK8sClient,boolean watchResource) {
			this.k8sClient = k8sClient;
			this.originalK8sClient = originalK8sClient;
			this.watchResource = watchResource;
		}
	}
}
