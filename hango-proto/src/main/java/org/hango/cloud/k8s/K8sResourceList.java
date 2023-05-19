package org.hango.cloud.k8s;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListMeta;

import java.util.List;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2021/9/10
 */
@JsonDeserialize
public class K8sResourceList<T extends HasMetadata> implements KubernetesResourceList<T> {

	private ListMeta metadata;
	private List<T> items;

	@Override
	public List<T> getItems() {
		return this.items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	@Override
	public ListMeta getMetadata() {
		return this.metadata;
	}

	public void setMetadata(ListMeta metadata) {
		this.metadata = metadata;
	}

}
