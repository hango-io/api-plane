package org.hango.cloud.cache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.protobuf.ProtocolStringList;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.Gateway;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.GatewayList;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.base.OperationContext;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.informers.cache.Indexer;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.k8s.K8sResourceApiEnum;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.k8s.K8sTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:37
**/
@Component
public class K8sResourceCache implements ResourceCache {
    private static final Logger log = LoggerFactory.getLogger(K8sResourceCache.class);

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;


    @Autowired
    private GlobalConfig globalConfig;

    private SharedInformerFactory sharedInformerFactory;

    private Map<String, Indexer<HasMetadata>> storeMap = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initInformer() {
        if (!multiClusterK8sClient.watchResource()){
            return;
        }
        Serialization.jsonMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(new ProtobufModule());
        //注册informer
        sharedInformerFactory = multiClusterK8sClient.getMasterOriginalClient().informers();
        registryInformer(K8sResourceApiEnum.VirtualService, K8sTypes.VirtualService.class, K8sTypes.VirtualServiceList.class);
        registryInformer(K8sResourceApiEnum.DestinationRule, K8sTypes.DestinationRule.class, K8sTypes.DestinationRuleList.class);
        registryInformer(K8sResourceApiEnum.EnvoyPlugin, K8sTypes.EnvoyPlugin.class, K8sTypes.EnvoyPluginList.class);
        registryInformer(K8sResourceApiEnum.SmartLimiter, K8sTypes.SmartLimiter.class, K8sTypes.SmartLimiterList.class);
        registryInformer(K8sResourceApiEnum.KubernetesGateway, Gateway.class, GatewayList.class);
        registryInformer(K8sResourceApiEnum.HTTPRoute, HTTPRoute.class, HTTPRouteList.class);
        sharedInformerFactory.startAllRegisteredInformers();

    }

    private <T extends HasMetadata, L extends KubernetesResourceList<T>> void registryInformer(K8sResourceApiEnum kind, Class<T> apiTypeClass, Class<L> apiListTypeClass){
        KubernetesClient masterOriginalClient = multiClusterK8sClient.getMasterOriginalClient();
        CustomResourceDefinition crd;
        try {
            crd = masterOriginalClient.customResourceDefinitions().withName(kind.getApi()).get();
        } catch (Exception e) {
            log.error("get crd definition error", e);
            return;
        }
        CustomResourceDefinitionContext customResourceDefinitionContext = CustomResourceDefinitionContext.fromCrd(crd);
        Indexer<T> indexer = sharedInformerFactory.sharedIndexInformerForCustomResource(
                customResourceDefinitionContext,
                apiTypeClass,
                apiListTypeClass,
                new OperationContext().withNamespace(globalConfig.getResourceNamespace()),
                60 * 1000L).getIndexer();
        storeMap.put(kind.name(), (Indexer<HasMetadata>) indexer);
    }



    @Override
    public List<HasMetadata> getResource(String kind) {
        if (!storeMap.containsKey(kind)){
            return new ArrayList<>();
        }
        return storeMap.get(kind).list().stream().filter(this::inNamespace).collect(Collectors.toList());
    }

    @Override
    public List<HasMetadata> getResourceByName(String kind, String name) {
        List<HasMetadata> resource = getResource(kind);
        if (StringUtils.isNotEmpty(name)){
            resource = resource.stream().filter(o -> name.equals(o.getMetadata().getName())).collect(Collectors.toList());
        }
        return resource;
    }

    @Override
    public List<HasMetadata> getResource(String gateway, String kind) {
        List<HasMetadata> resource = getResource(kind);
        return resource.stream().filter(o -> inGateway(o, gateway)).collect(Collectors.toList());
    }

    @Override
    public List<String> getResourceName(String kind) {
        if (!storeMap.containsKey(kind)){
            return new ArrayList<>();
        }
        return storeMap.get(kind).listKeys();
    }

    private boolean inNamespace(HasMetadata hasMetadata){
        return hasMetadata.getMetadata().getNamespace().equals(globalConfig.getResourceNamespace());
    }

    private boolean inGateway(HasMetadata hasMetadata, String gateway){
        if (StringUtils.isEmpty(gateway)){
            return true;
        }
        switch (K8sResourceApiEnum.getByName(hasMetadata.getKind())){
            case DestinationRule:
            case VirtualService:
                return hasMetadata.getMetadata().getName().endsWith(gateway);
            case EnvoyPlugin:
                return matchPluginGateway(hasMetadata, gateway);
            default:
                log.error("错误的资源类型：{}", hasMetadata.getKind());
                return false;
        }
    }

    private boolean matchPluginGateway(HasMetadata hasMetadata, String code){
        K8sTypes.EnvoyPlugin plugin = (K8sTypes.EnvoyPlugin)hasMetadata;
        ProtocolStringList gatewayList = plugin.getSpec().getGatewayList();
        if (gatewayList.size() == 0){
            return false;
        }
        String gateway = gatewayList.get(0);
        if (gateway.contains("/")){
            return StringUtils.equals(gateway.split("/")[1], code);
        }
        return false;
    }
}

