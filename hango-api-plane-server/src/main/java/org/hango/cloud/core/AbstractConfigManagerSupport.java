package org.hango.cloud.core;

import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.k8s.empty.EmptyResource;
import org.hango.cloud.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

public abstract class AbstractConfigManagerSupport implements ConfigManager{

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigManagerSupport.class);

    protected void update(ConfigStore configStore, List<K8sResourcePack> resources, IstioModelEngine modelEngine) {
        if (CollectionUtils.isEmpty(resources)) return;

        for (K8sResourcePack pack : resources) {
            HasMetadata latest = pack.getResource();

            HasMetadata old = configStore.get(latest);
            if (old != null) {
                if (old.equals(latest)) continue;
                HasMetadata merged;

                if (latest instanceof EmptyResource && pack.hasSubtracter()) {
                    // 若latest标识为emptyResource, 则进行subtract操作
                    merged = pack.getSubtracter().subtract(old);
                } else if (pack.hasMerger()) {
                    merged = pack.getMerger().merge(old, latest);
                } else {
                    merged = modelEngine.merge(old, latest);
                }

                handle(merged, configStore, modelEngine);
                continue;
            }
            handle(latest, configStore, modelEngine);
        }
    }

    protected void delete(ConfigStore configStore, List<K8sResourcePack> resources, IstioModelEngine modelEngine) {
        delete(resources, (i1, i2) -> 0, n -> n, configStore, modelEngine);
    }

    protected void delete(List<K8sResourcePack> packs, Comparator<K8sResourcePack> compartor, Subtracter<HasMetadata> fun, ConfigStore configStore, IstioModelEngine modelEngine) {
        if (CollectionUtils.isEmpty(packs)) return;
        for (K8sResourcePack pack : packs) {
            HasMetadata resource = pack.getResource();
            HasMetadata exist = configStore.get(resource);
            if (exist != null) {
                pack.setResource(exist);
            }
        }
        packs.stream()
                .sorted(compartor)
                .map(p -> {
                    HasMetadata resource = p.getResource();
                    if (p.hasSubtracter()) {
                        return p.getSubtracter().subtract(resource);
                    }
                    if (fun != null) {
                        return fun.subtract(resource);
                    }
                    return resource;
                })
                .filter(i -> i != null)
                .forEach(r -> handle(r, configStore, modelEngine));
    }

    private void handle(HasMetadata i, ConfigStore configStore, IstioModelEngine modelEngine) {
        if (modelEngine.isUseless(i) || i instanceof EmptyResource) {
            try {
                configStore.delete(i);
                deleteNotification(i);
            } catch (Exception e) {
                //ignore error
                logger.warn("", e);
            }
        } else {
            configStore.update(i);
        }
    }

    protected abstract void deleteNotification(HasMetadata i);

}
