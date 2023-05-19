package org.hango.cloud.core;


import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Map;

/**
 * 配置持久化
 */
public interface ConfigStore {

    void delete(HasMetadata t);

    void update(HasMetadata t);

    HasMetadata get(HasMetadata t);

    HasMetadata get(String kind, String namespace, String name);

    List<HasMetadata> get(String kind, String namespace);

    List<HasMetadata> get(String kind, String namespace, Map<String, String> labels);

}
