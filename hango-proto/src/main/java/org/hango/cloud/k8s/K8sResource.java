package org.hango.cloud.k8s;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2021/9/9
 */
@JsonDeserialize
public class K8sResource<T> implements HasMetadata {

	private static final Map<String, String> mp = new HashMap<>();

	static {
		try {
			Class.forName(K8sTypes.class.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private ObjectMeta metadata;
	private final String kind;
	private final String apiVersion;
	private T spec;

	public K8sResource() {
		this.kind = getClass().getSimpleName();
		this.apiVersion = mp.get(kind);
		if (apiVersion == null) {
			throw new IllegalStateException(String.format("'%s' is not registered. Please register this class in %s like: \n" +
				"K8sResource.addKind(\"microservice.slime.io/v1alpha1\", RichVirtualService.class);", kind, K8sTypes.class.getName()));
		}
	}

	public <ThisType extends K8sResource<T>> ThisType with(String name, String namespace, T spec) {
		if (metadata == null) {
			metadata = new ObjectMeta();
		}
		metadata.setName(name);
		metadata.setNamespace(namespace);
		this.spec = spec;
		return (ThisType) this;
	}

	@Override
	public ObjectMeta getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(ObjectMeta objectMeta) {
		this.metadata = objectMeta;
	}

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getApiVersion() {
		return apiVersion;
	}

	public void setKind(String kind) {
		//用于应付json序列化工具, 实际上kind不需要设置
	}

	@Override
	public void setApiVersion(String apiVersion) {
		//用于应付hasMetadata接口及json序列化工具, 实际上apiVersion不需要设置
	}

	public T getSpec() {
		return spec;
	}

	public void setSpec(T spec) {
		this.spec = spec;
	}

	public static void addKind(String apiVersion, Class<? extends KubernetesResource> clazz) {
		try {
			String kind = clazz.getSimpleName();
			mp.put(kind, apiVersion);
			KubernetesDeserializer.registerCustomKind(apiVersion, kind, clazz);
			KubernetesDeserializer.registerCustomKind(apiVersion, kind + "List", (Class<? extends KubernetesResource>) Class.forName(clazz.getName() + "List"));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


}
