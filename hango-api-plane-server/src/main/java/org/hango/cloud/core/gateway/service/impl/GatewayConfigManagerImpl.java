package org.hango.cloud.core.gateway.service.impl;

import org.hango.cloud.core.AbstractConfigManagerSupport;
import org.hango.cloud.core.ConfigStore;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.PathExpressionEnum;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.gateway.GatewayIstioModelEngine;
import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.k8s.event.K8sResourceDeleteNotificationEvent;
import org.hango.cloud.core.k8s.subtracter.ServiceEntryEndpointsSubtracter;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.IstioGateway;
import org.hango.cloud.meta.PluginOrder;
import org.hango.cloud.meta.Service;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import org.hango.cloud.meta.GatewayPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GatewayConfigManagerImpl extends AbstractConfigManagerSupport implements
    GatewayConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfigManagerImpl.class);

    private ConfigStore configStore;
    private GatewayIstioModelEngine modelEngine;
    private GlobalConfig globalConfig;
    private ApplicationEventPublisher eventPublisher;

    public GatewayConfigManagerImpl(GatewayIstioModelEngine modelEngine, ConfigStore k8sConfigStore, GlobalConfig globalConfig, ApplicationEventPublisher eventPublisher) {
        this.modelEngine = modelEngine;
        this.configStore = k8sConfigStore;
        this.globalConfig = globalConfig;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void updateConfig(API api) {
        List<K8sResourcePack> resources = modelEngine.translate(api);
        update(resources);
    }

    @Override
    public void updateConfig(GatewayPlugin plugin) {
        List<K8sResourcePack> resources = modelEngine.translate(plugin);
        update(resources);
    }

    @Override
    public void updateConfig(Service service) {
        List<K8sResourcePack> resources = modelEngine.translate(service);
        update(resources);
    }

    private void update(List<K8sResourcePack> resources) {
        update(configStore, resources, modelEngine);
    }

    @Override
    public void deleteConfig(API api) {
        List<K8sResourcePack> resources = modelEngine.translate(api, true);
        delete(resources);
    }

    @Override
    public void deleteConfig(Service service) {
        if (StringUtils.isEmpty(service.getGateway())) return;
        List<K8sResourcePack> resources = modelEngine.translate(service);
        Class<? extends HasMetadata> drClz = K8sResourceEnum.DestinationRule.mappingType();
        Class<? extends HasMetadata> seClz = K8sResourceEnum.ServiceEntry.mappingType();

        //move dr to head
        Comparator<K8sResourcePack> compareFun = (p1, p2) -> {
            if (p1.getResource().getClass() == drClz) {
                return -1;
            } else if (p2.getResource().getClass() == drClz) {
                return 1;
            }
            return 0;
        };

        Set<String> subsets = new HashSet<>();
        if (!CollectionUtils.isEmpty(service.getSubsets())) {
            subsets.addAll(service.getSubsets().stream().map(s -> s.getName()).collect(Collectors.toSet()));
        }
        Subtracter<HasMetadata> deleteFun = new Subtracter<HasMetadata>() {
            //dr若不存在subset，则全部删除相关资源
            boolean noSubsets = false;

            @Override
            public HasMetadata subtract(HasMetadata r) {
                if (r.getClass() == drClz) {
                    ResourceGenerator gen = ResourceGenerator.newInstance(r, ResourceType.OBJECT);
                    // 如果没有传入subset，则删除默认subset
                    if (CollectionUtils.isEmpty(subsets)) {
                        subsets.add(String.format("%s-%s", service.getCode(), service.getGateway()));
                    }
                    subsets.forEach(ss -> gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_NAME.translate(ss)));
                    K8sTypes.DestinationRule dr = gen.object(K8sTypes.DestinationRule.class);
                    if (dr.getSpec().getSubsetsCount() == 0) noSubsets = true;
                    return dr;
                } else if (r.getClass() == seClz) {
                    // 若没有subset，则删除整个se
                    if (noSubsets) {
                        r.setApiVersion(null);
                    } else {
                        ServiceEntryEndpointsSubtracter sub = new ServiceEntryEndpointsSubtracter(service.getGateway());
                        return sub.subtract((ServiceEntry) r);
                    }
                } else {
                    if (noSubsets) r.setApiVersion(null);
                }
                return r;
            }
        };
        delete(resources, compareFun, deleteFun, configStore, modelEngine);
    }

    @Override
    public HasMetadata getConfig(PluginOrder pluginOrder) {
        List<K8sResourcePack> resources = modelEngine.translate(pluginOrder);
        if (CollectionUtils.isEmpty(resources) || resources.size() != 1) throw new ApiPlaneException();
        return configStore.get(resources.get(0).getResource());
    }

    @Override
    public void updateConfig(PluginOrder pluginOrder) {
        List<K8sResourcePack> resources = modelEngine.translate(pluginOrder);
        update(resources);
    }

    @Override
    public void deleteConfig(PluginOrder pluginOrder) {
        List<K8sResourcePack> resources = modelEngine.translate(pluginOrder);
        delete(resources, clearResource());
    }

    @Override
    public HasMetadata getConfig(IstioGateway istioGateway) {
        if (StringUtils.isEmpty(istioGateway.getGwCluster())) {
            return null;
        }
        final String gwClusgterKey = "gw_cluster";
        //获取所有Gateway资源
        List<HasMetadata> istioResources = configStore.get("Gateway", globalConfig.getResourceNamespace());
        Optional<HasMetadata> first = istioResources.stream().filter(g ->
        {
            IstioResource ir = (IstioResource) g;
            GatewaySpec spec = (GatewaySpec) ir.getSpec();
            if (spec == null) {
                return false;
            }
            Map<String, String> selector = spec.getSelector();
            if (selector == null) {
                return false;
            }
            return istioGateway.getGwCluster().equals(selector.get(gwClusgterKey));
        }).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return null;
    }

    @Override
    public void updateConfig(IstioGateway istioGateway) {
        List<K8sResourcePack> resources = modelEngine.translate(istioGateway);
        update(resources);
    }

    private void delete(List<K8sResourcePack> resources, Subtracter<HasMetadata> fun) {
        delete(resources, (i1, i2) -> 0, fun, configStore, modelEngine);
    }

    private void delete(List<K8sResourcePack> resources) {
        delete(resources, (i1, i2) -> 0, null, configStore, modelEngine);
    }

    private Subtracter<HasMetadata> clearResource() {
        return resource -> {
            resource.setApiVersion(null);
            return resource;
        };
    }


    @Override
    protected void deleteNotification(HasMetadata i) {
        eventPublisher.publishEvent(new K8sResourceDeleteNotificationEvent(i));
    }
}
