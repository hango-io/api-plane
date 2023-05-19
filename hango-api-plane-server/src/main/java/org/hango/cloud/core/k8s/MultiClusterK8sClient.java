package org.hango.cloud.core.k8s;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.configuration.ext.K8sMultiClusterProperties;
import org.hango.cloud.core.editor.EditorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static okhttp3.TlsVersion.TLS_1_1;
import static okhttp3.TlsVersion.TLS_1_2;

public class MultiClusterK8sClient {
	private static final Logger logger = LoggerFactory.getLogger(MultiClusterK8sClient.class);


	public static final String MASTER_CLUSTER_NAME = "master";
	//主节点client
	private ClientSet masterClient = null;
	//从节点clients
	private List<KubernetesClient> nodeClients = new ArrayList<>();

	public MultiClusterK8sClient(K8sMultiClusterProperties properties, EditorContext editorContext) {
		Map<String, K8sMultiClusterProperties.K8sClusterConfig> clusters = properties.getClusters();
		//必须指定主机群
		if (clusters == null || !clusters.containsKey(MASTER_CLUSTER_NAME)) {
			throw new IllegalStateException("Must have master cluster");
		}
		for (Map.Entry<String, K8sMultiClusterProperties.K8sClusterConfig> entry : clusters.entrySet()) {
			K8sMultiClusterProperties.K8sClusterConfig value = entry.getValue();
			Config config = getConfig(entry.getValue());
			logger.info("start to connect {}, address:{}", entry.getKey(), value.getK8sApiServer());
			OkHttpClient httpClient = HttpClientUtils.createHttpClient(config);
			KubernetesClient k8sClient = new KubernetesClient(config, httpClient, editorContext);
			k8sClient.setClientName(entry.getKey());
			logger.info("connected {}, address:{}", entry.getKey(), entry.getValue().getK8sApiServer());
			if (MASTER_CLUSTER_NAME.equals(entry.getKey())){
				io.fabric8.kubernetes.client.KubernetesClient originalK8sClient = new DefaultKubernetesClient(httpClient, config);
				masterClient = new ClientSet(k8sClient, originalK8sClient, entry.getValue().isWatchResource());
			}else {
				nodeClients.add(k8sClient);
			}

		}
	}

	private static Config getConfig(K8sMultiClusterProperties.K8sClusterConfig clusterConfig) {
		if (StringUtils.isEmpty(clusterConfig.getK8sApiServer())){
			return Config.autoConfigure(null);
		}
		return new ConfigBuilder()
				.withMasterUrl(clusterConfig.getK8sApiServer())
				.withTrustCerts(true)
				.withDisableHostnameVerification(true)
				.withCaCertData(clusterConfig.getCaData())
				.withClientCertData(clusterConfig.getCertData())
				.withClientKeyData(clusterConfig.getKeyData()).withCaCertData(clusterConfig.getCaData())
				.withClientKeyPassphrase("passphrase")
				.withWatchReconnectInterval(5000)
				.withWatchReconnectLimit(5)
				.withRequestTimeout(5000)
				.withTlsVersions(TLS_1_2, TLS_1_1)
				.build();

	}

	/**
	 * Created by 张武(zhangwu@corp.netease.com) at 2019/11/13
	 */
	public static class ClientSet {
		public final KubernetesClient k8sClient;
		public final io.fabric8.kubernetes.client.KubernetesClient originalK8sClient;
		public final boolean watchResource;

		private ClientSet(KubernetesClient k8sClient, io.fabric8.kubernetes.client.KubernetesClient originalK8sClient,boolean watchResource) {
			this.k8sClient = k8sClient;
			this.originalK8sClient = originalK8sClient;
			this.watchResource = watchResource;
		}
	}

	public KubernetesClient getMasterClient() {
		return masterClient.k8sClient;
	}

	public io.fabric8.kubernetes.client.KubernetesClient getMasterOriginalClient() {
		return masterClient.originalK8sClient;
	}

	public boolean watchResource() {
		return masterClient.watchResource;
	}

	public List<KubernetesClient> getNodeClients() {
		return nodeClients;
	}
}
