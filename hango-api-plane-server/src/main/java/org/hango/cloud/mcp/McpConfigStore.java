package org.hango.cloud.mcp;

import org.hango.cloud.core.ConfigStore;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.mcp.dao.ResourceDao;
import org.hango.cloud.mcp.dao.meta.Resource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/21
 **/
public class McpConfigStore implements ConfigStore {

    private static final Logger logger = LoggerFactory.getLogger(McpConfigStore.class);

    private static final EnumSet<K8sResourceEnum> supportedResourceKind = EnumSet.of(
            K8sResourceEnum.VirtualService,
            K8sResourceEnum.DestinationRule,
            K8sResourceEnum.Gateway,
            K8sResourceEnum.EnvoyPlugin,
            K8sResourceEnum.PluginManager,
            K8sResourceEnum.ServiceEntry,
            K8sResourceEnum.ConfigMap
    );

    private ResourceDao resourceDao;
    private GlobalConfig globalConfig;

    public McpConfigStore(ResourceDao resourceDao, GlobalConfig globalConfig) {
        this.resourceDao = resourceDao;
        this.globalConfig = globalConfig;
    }

    @Override
    public void delete(HasMetadata t) {
        if (isUnSupportedType(t.getKind())) return;
        supply(t);
        String collection = getCollectionByKind(t.getKind());
        String name = McpUtils.getResourceName(t.getMetadata().getNamespace(), t.getMetadata().getName());
        resourceDao.delete(collection, name);
    }

    @Override
    public void update(HasMetadata t) {
        if (isUnSupportedType(t.getKind())) return;
        supply(t);
        Resource rs = toResource(t);
        String collection = getCollectionByKind(t.getKind());
        String name = McpUtils.getResourceName(t.getMetadata().getNamespace(), t.getMetadata().getName());
        if (resourceDao.contains(collection, name)) {
            resourceDao.update(rs);
        } else {
            resourceDao.add(rs);
        }
    }

    @Override
    public HasMetadata get(HasMetadata t) {
        if (isUnSupportedType(t.getKind())) return null;
        supply(t);
        return get(t.getKind(), t.getMetadata().getNamespace(), t.getMetadata().getName());
    }

    @Override
    public HasMetadata get(String kind, String namespace, String name) {
        if (isUnSupportedType(kind)) return null;
        String collection = getCollectionByKind(kind);
        String resourceName = McpUtils.getResourceName(namespace, name);
        Resource rs = resourceDao.get(collection, resourceName);
        if (Objects.isNull(rs)) {
            return null;
        }
        return toHasMetadata(rs);
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace) {
        if (isUnSupportedType(kind)) return new ArrayList<>();
        List<HasMetadata> ret = new ArrayList<>();
        String collection = getCollectionByKind(kind);
        List<Resource> rss = resourceDao.list(collection, namespace);
        rss.forEach(rs -> ret.add(toHasMetadata(rs)));
        return ret;
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace, Map<String, String> labels) {
        if (isUnSupportedType(kind)) return new ArrayList<>();
        List<HasMetadata> ret = new ArrayList<>();
        String collection = getCollectionByKind(kind);
        String labelString = McpUtils.getLabelMatch(labels);
        List<Resource> rss = resourceDao.list(collection, namespace, labelString);
        rss.forEach(rs -> ret.add(toHasMetadata(rs)));
        return ret;
    }

    private boolean isUnSupportedType(String kind) {
        if (!supportedResourceKind.contains(K8sResourceEnum.get(kind))) {
            logger.warn("MCP: unsupported type for kind:{}", kind);
            return true;
        }
        return false;
    }

    private Resource toResource(HasMetadata hasMetadata) {
        String json = ResourceGenerator.obj2json(hasMetadata);
        String name = McpUtils.getResourceName(hasMetadata.getMetadata().getNamespace(), hasMetadata.getMetadata().getName());
        String label = McpUtils.getLabel(hasMetadata.getMetadata().getLabels());
        String collection = getCollectionByKind(hasMetadata.getKind());
        Resource resource = new Resource();
        resource.setConfig(json);
        resource.setName(name);
        resource.setLabel(label);
        resource.setCollection(collection);
        return resource;
    }

    private HasMetadata toHasMetadata(Resource resource) {
        String kind = McpResourceEnum.getByCollection(resource.getCollection()).name();
        return ResourceGenerator.json2obj(resource.getConfig(), K8sResourceEnum.get(kind).mappingType());
    }

    private String getCollectionByKind(String kind) {
        return McpResourceEnum.get(kind).getCollection();
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
            oldLabels.put("skiff-api-plane-type", globalConfig.getApiPlaneType());
            oldLabels.put("skiff-api-plane-version", globalConfig.getApiPlaneVersion());
            metadata.setLabels(oldLabels);
        }
    }

    boolean isGlobalCrd(HasMetadata resource) {
        return K8sResourceEnum.get(resource.getKind()).isClustered();
    }
}
