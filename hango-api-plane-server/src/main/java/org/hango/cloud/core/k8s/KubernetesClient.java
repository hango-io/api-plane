package org.hango.cloud.core.k8s;

import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.http.DefaultK8sHttpClient;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Config;
import okhttp3.OkHttpClient;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class KubernetesClient extends DefaultK8sHttpClient {

    public KubernetesClient(Config config, OkHttpClient httpClient, EditorContext editorContext) {
        super(config, httpClient, editorContext);
    }

    /**
     * 当资源不存在时，会返回null
     *
     * @param kind      资源类型
     * @param namespace 命名空间
     * @param name      资源名
     * @return json
     */
    public String get(String kind, String namespace, String name) {
        String url = getUrl(kind, namespace, name);
        return getWithNull(url);
    }

    /**
     * 返回的对象类型根据K8sResourceEnum中映射的mappingType
     *
     * @param kind      资源类型
     * @param namespace 命名空间
     * @param name      资源名
     * @param <T>       返回对象类型
     * @return 资源对象
     */
    public <T> T getObject(String kind, String namespace, String name) {
        String url = getUrl(kind, namespace, name);
        return getObject(url);
    }

    /**
     * 返回的对象类型根据K8sResourceEnum中映射的mappingType
     *
     * @param kind      资源类型
     * @param namespace 命名空间
     * @param <T>       返回对象类型
     * @return 资源对象数组
     */
    public <T> List<T> getObjectList(String kind, String namespace) {
        String url = getUrl(kind, namespace);
        return getObjectList(url);
    }

    /**
     * @param kind      资源类型
     * @param namespace 命名空间
     * @param name      资源名
     * @param labels    标签
     * @param <T>       返回资源类型
     * @return 资源对象
     */
    public <T> T getObject(String kind, String namespace, String name, Map<String, String> labels) {
        String url = getUrlWithLabels(kind, namespace, name, labels);
        return getObject(url);
    }

    /**
     * @param kind      资源类型
     * @param namespace 命名空间
     * @param labels    标签
     * @param <T>       返回资源类型
     * @return 资源对象数组
     */
    public <T> List<T> getObjectList(String kind, String namespace, Map<String, String> labels) {
        String url = getUrlWithLabels(kind, namespace, labels);
        return getObjectList(url);
    }

    /**
     * 当资源不存在时，会返回null
     *
     * @param url 请求url
     * @param <T> 返回资源类型
     * @return 资源对象
     */
    public <T> T getObject(String url) {
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;

        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        return (T) gen.object(resourceEnum.mappingType());
    }

    /**
     * 当资源不存在时，会返回null
     *
     * @param url 请求url
     * @param <T> 返回资源类型
     * @return 资源对象数组
     */
    public <T> List<T> getObjectList(String url) {
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;

        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.getItem(gen.getKind());
        return gen.object(resourceEnum.mappingListType()).getItems();
    }

    public KubernetesResourceList getListObject(String kind, String namespace) {
        String url = getUrl(kind, namespace);
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;

        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.getItem(gen.getKind());
        return  gen.object(resourceEnum.mappingListType());
    }

    /**
     * 创建或更新资源
     *
     * @param obj          资源对象
     * @param resourceType 资源类型
     */
    public void createOrUpdate(Object obj, ResourceType resourceType) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, resourceType);
        String url = getUrl(gen.getKind(), gen.getNamespace(), gen.getName());
        String oldResource = getWithNull(url);

        if (oldResource != null) {
            K8sResourceGenerator oldGenerator = K8sResourceGenerator.newInstance(oldResource, ResourceType.JSON);
            gen.setResourceVersion(resourceVersionGenerator(oldGenerator.getResourceVersion()));
            put(url, gen.jsonString());
            return;
        }
        post(getUrl(gen.getKind(), gen.getNamespace()), gen.jsonString());
    }

    /**
     * 删除资源
     *
     * @param kind      资源类型
     * @param namespace 命名空间
     * @param name      资源名
     */
    public void delete(String kind, String namespace, String name) {
        String url = getUrl(kind, namespace, name);
        delete(url);
    }

    private String resourceVersionGenerator(String oldResourceVersion) {
        return oldResourceVersion;
    }

}
