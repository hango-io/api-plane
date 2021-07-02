package org.hango.cloud.configuration.ext;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties("k8s")
@Component
public class K8sMultiClusterProperties {

	private Map<String, K8sClusterConfig> clusters;

	public Map<String, K8sClusterConfig> getClusters() {
		return clusters;
	}

	public void setClusters(Map<String, K8sClusterConfig> clusters) {
		this.clusters = clusters;
	}

	public static class K8sClusterConfig {
		private String k8sApiServer;
		private String certData;
		private String keyData;
		private boolean watchResource = true;


		public boolean isWatchResource() {
			return watchResource;
		}

		public void setWatchResource(boolean watchResource) {
			this.watchResource = watchResource;
		}

		public String getK8sApiServer() {
			return k8sApiServer;
		}

		public void setK8sApiServer(String k8sApiServer) {
			this.k8sApiServer = k8sApiServer;
		}

		public String getCertData() {
			return certData;
		}

		public void setCertData(String certData) {
			this.certData = certData;
		}

		public String getKeyData() {
			return keyData;
		}

		public void setKeyData(String keyData) {
			this.keyData = keyData;
		}
	}

}
