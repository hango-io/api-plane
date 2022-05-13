package org.hango.cloud.mcp.snapshot;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.hango.cloud.mcp.McpMarshaller;
import org.hango.cloud.mcp.McpOptions;
import org.hango.cloud.mcp.McpResourceEnum;
import org.hango.cloud.mcp.McpUtils;
import org.hango.cloud.mcp.dao.ResourceDao;
import org.hango.cloud.mcp.dao.meta.Resource;
import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.Mcp;
import istio.mcp.v1alpha1.MetadataOuterClass;
import istio.mcp.v1alpha1.ResourceOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/22
 **/
public class DBSnapshotBuilder implements SnapshotBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DBSnapshotBuilder.class);

    private ResourceDao resourceDao;
    private McpMarshaller mcpMarshaller;
    private McpOptions mcpOptions;

    public DBSnapshotBuilder(ResourceDao resourceDao, McpMarshaller mcpMarshaller, McpOptions mcpOptions) {
        this.resourceDao = resourceDao;
        this.mcpMarshaller = mcpMarshaller;
        this.mcpOptions = mcpOptions;
    }

    @Override
    public SnapshotOuterClass.Snapshot build() {
        SnapshotOuterClass.Snapshot.Builder builder = SnapshotOuterClass.Snapshot.newBuilder();
        Map<String, Mcp.Resources.Builder> resourcesMap = new HashMap<>();
        for (String collection : mcpOptions.getSnapshotCollections()) {
            // 1. 取出对应collection资源
            List<Resource> resources = resourceDao.list(collection);
            // 2. 将Resource转化为ResourceOuterClass.Resource，用parallelStream提升性能
            Collection<ResourceOuterClass.Resource> rsList = resources.parallelStream().map(item -> getResource(item.getConfig())).collect(Collectors.toList());
            Mcp.Resources.Builder rsBuilder = Mcp.Resources.newBuilder();
            rsBuilder.addAllResources(rsList);
            resourcesMap.put(collection, rsBuilder);
        }

        // 3. 为Resources设置Version，并add到Snapshot
        for (Map.Entry<String, Mcp.Resources.Builder> entry : resourcesMap.entrySet()) {
            Mcp.Resources.Builder rsBuilder = entry.getValue();
            rsBuilder.setSystemVersionInfo(productCollectionVersion(rsBuilder));
            rsBuilder.setCollection(entry.getKey());
            builder.putResources(entry.getKey(), rsBuilder.build());
        }

        builder.setVersion(new Date().toString());
        return builder.build();
    }

    private String productCollectionVersion(Mcp.Resources.Builder resources) {
        return String.format("count:[%s]", resources.getResourcesList());
    }

    public ResourceOuterClass.Resource getResource(String json) {
        K8sResourceGenerator itemGen = K8sResourceGenerator.newInstance(json);
        Object spec = itemGen.getSpec();
        String kind = itemGen.getKind();
        String resourceName = McpUtils.getResourceName(itemGen.getNamespace(), itemGen.getName());
        String version = itemGen.getResourceVersion();
        String createTime = itemGen.getCreateTimestamp();
        if (Objects.isNull(spec)) {
            logger.warn("Resource :{} has not spec", resourceName);
            //todo: 处理不带spec的资源
        }
        return getResource(ResourceGenerator.obj2json(spec), kind, resourceName, version, createTime, itemGen.getLabels(), itemGen.getAnnotations());
    }

    public ResourceOuterClass.Resource getResource(String json,
                                                   String kind,
                                                   String name,
                                                   String version,
                                                   String createTime,
                                                   Map<String, String> labels,
                                                   Map<String, String> annotations) {
        MetadataOuterClass.Metadata.Builder metadata = MetadataOuterClass.Metadata
                .newBuilder()
                .setName(name);
        if (Objects.nonNull(version)) {
            metadata.setVersion(version);
        }
        if (Objects.nonNull(createTime)) {
            try {
                metadata.setCreateTime(Timestamps.parse(createTime));
            } catch (ParseException e) {
                logger.warn("parse timestamp {} error:{}", createTime, e);
            }
        }
        if (Objects.nonNull(labels)) {
            metadata.putAllLabels(labels);
        }
        if (Objects.nonNull(annotations)) {
            metadata.putAllAnnotations(annotations);
        }

        try {
            Message.Builder builder = McpResourceEnum.get(kind).getInstance().newBuilderForType();
            mcpMarshaller.merge(json, builder);

            return ResourceOuterClass.Resource
                    .newBuilder()
                    .setMetadata(metadata)
                    .setBody(Any.pack(builder.build()))
                    .build();
        } catch (Exception e) {
            logger.warn("Marshal json to proto failure. config:{}, json:{}", name, json);
            throw e;
        }
    }
}
