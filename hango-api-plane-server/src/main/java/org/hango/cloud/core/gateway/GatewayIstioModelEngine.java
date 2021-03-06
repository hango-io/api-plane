package org.hango.cloud.core.gateway;

import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.IstioModelEngine;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.gateway.processor.DefaultModelProcessor;
import org.hango.cloud.core.gateway.processor.NeverReturnNullModelProcessor;
import org.hango.cloud.core.gateway.processor.RenderTwiceModelProcessor;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.k8s.empty.DynamicGatewayPluginSupplier;
import org.hango.cloud.core.k8s.empty.EmptyConfigMap;
import org.hango.cloud.core.k8s.merger.GatewayRateLimitConfigMapMerger;
import org.hango.cloud.core.k8s.operator.IntegratedResourceOperator;
import org.hango.cloud.core.k8s.subtracter.GatewayPluginNormalSubtracter;
import org.hango.cloud.core.k8s.subtracter.GatewayRateLimitConfigMapSubtracter;
import org.hango.cloud.core.k8s.subtracter.GatewayVirtualServiceSubtracter;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateTranslator;
import org.hango.cloud.service.PluginService;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.constant.LogConstant;
import org.hango.cloud.util.constant.PluginConstant;
import io.fabric8.kubernetes.api.model.HasMetadata;
import istio.networking.v1alpha3.VirtualServiceOuterClass;
import org.hango.cloud.core.gateway.handler.GatewayPluginConfigMapDataHandler;
import org.hango.cloud.core.gateway.handler.GatewayPluginDataHandler;
import org.hango.cloud.core.gateway.handler.PluginOrderDataHandler;
import org.hango.cloud.core.gateway.handler.PortalDestinationRuleServiceDataHandler;
import org.hango.cloud.core.gateway.handler.PortalGatewayDataHandler;
import org.hango.cloud.core.gateway.handler.PortalServiceEntryServiceDataHandler;
import org.hango.cloud.core.gateway.handler.PortalVirtualServiceAPIDataHandler;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.GatewayPlugin;
import org.hango.cloud.meta.IstioGateway;
import org.hango.cloud.meta.PluginOrder;
import org.hango.cloud.meta.Service;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.k8s.K8sTypes.VirtualService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GatewayIstioModelEngine extends IstioModelEngine {

    private static final Logger logger = LoggerFactory.getLogger(IstioModelEngine.class);

    private IntegratedResourceOperator operator;
    private TemplateTranslator templateTranslator;
    private EditorContext editorContext;
    private ResourceManager resourceManager;
    private PluginService pluginService;
    private GlobalConfig globalConfig;

    @Autowired
    public GatewayIstioModelEngine(IntegratedResourceOperator operator, TemplateTranslator templateTranslator, EditorContext editorContext,
                                   ResourceManager resourceManager, PluginService pluginService, GlobalConfig globalConfig) {
        super(operator);
        this.operator = operator;
        this.templateTranslator = templateTranslator;
        this.editorContext = editorContext;
        this.resourceManager = resourceManager;
        this.pluginService = pluginService;
        this.globalConfig = globalConfig;

        this.defaultModelProcessor = new DefaultModelProcessor(templateTranslator);
        this.renderTwiceModelProcessor = new RenderTwiceModelProcessor(templateTranslator);
        this.neverNullRenderTwiceProcessor = new NeverReturnNullModelProcessor(this.renderTwiceModelProcessor, NEVER_NULL);
    }

    private DefaultModelProcessor defaultModelProcessor;
    private RenderTwiceModelProcessor renderTwiceModelProcessor;
    private NeverReturnNullModelProcessor neverNullRenderTwiceProcessor;

    @Value(value = "${http10:#{null}}")
    Boolean enableHttp10;

    @Value(value = "${rateLimitConfigMapName:rate-limit-config}")
    String rateLimitConfigMapName;

    @Value(value = "${rateLimitNameSpace:gateway-system}")
    String rateLimitNamespace;


    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String rateLimitConfigMap = "gateway/api/rateLimitConfigMap";

    private static final String serviceDestinationRule = "gateway/service/destinationRule";
    private static final String pluginManager = "gateway/pluginManager";
    private static final String serviceServiceEntry = "gateway/service/serviceEntry";
    private static final String gatewayPlugin = "gateway/gatewayPlugin";
    private static final String VIRTUAL_SERVICE = "VirtualService";

    public List<K8sResourcePack> translate(API api) {
        return translate(api, false);
    }

    /**
     * ???api?????????istio???????????????
     *
     * @param api    ????????????
     * @param simple ??????????????????????????????????????????????????????????????????
     * @return K8s????????????
     */
    public List<K8sResourcePack> translate(API api, boolean simple) {
        logger.info("{}{} start translate k8s resource", LogConstant.TRANSLATE_LOG_NOTE, LogConstant.ROUTE_LOG_NOTE);
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        api.setNamespace(globalConfig.getResourceNamespace());
        List<FragmentWrapper> vsFragments = new ArrayList<>();
        // ??????VS?????????????????????????????????Match?????????
        if (!StringUtils.isEmpty(api.getPlugins())) {
            vsFragments = renderPlugins(api.getPlugins()).stream()
                    .map(FragmentHolder::getVirtualServiceFragment)
                    .collect(Collectors.toList());
        }
        List<String> rawVirtualServices = renderTwiceModelProcessor
                .process(apiVirtualService, api,
                        new PortalVirtualServiceAPIDataHandler(
                                defaultModelProcessor, vsFragments, simple));

        logger.info("{}{} start to generate and add k8s resource",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.ROUTE_LOG_NOTE);
        resourcePacks.addAll(generateK8sPack(rawVirtualServices,
                new GatewayVirtualServiceSubtracter(api.getName()),
                r -> r, this::adjust));
        logger.info("{}{} raw virtual services added ok", LogConstant.TRANSLATE_LOG_NOTE, LogConstant.ROUTE_LOG_NOTE);

        return resourcePacks;
    }

    /**
     * ??????GatewayPlugin?????????CRD??????
     *
     * @param plugin ??????????????????????????????????????????
     * @return k8s????????????
     */
    public List<K8sResourcePack> translate(GatewayPlugin plugin) {
        logger.info("{}{} start translate k8s resource", LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);

        // ????????????????????????
        plugin.showPluginConfigsInLog(logger);

        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(plugin.getPlugins()));

        logger.info("{}{} render plugins ok, start to generate and add k8s resource",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
        return generateAndAddK8sResource(rawResourceContainer, plugin);
    }

    /**
     * ??????k8s????????????????????????k8s????????????
     *
     * @param rawResourceContainer k8s??????????????????????????????
     * @param plugin               ??????????????????????????????????????????
     * @return k8s????????????
     */
    private List<K8sResourcePack> generateAndAddK8sResource(RawResourceContainer rawResourceContainer,
                                                            GatewayPlugin plugin) {
        // ????????????GatewayPlugin??????
        logger.info("{}{} start render raw GatewayPlugin CRDs",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
        List<K8sResourcePack> resourcePacks = configureGatewayPlugin(rawResourceContainer, plugin);

        // ????????????????????????????????????ConfigMap??????
        if (isNeedToRenderConfigMap(plugin)) {
            logger.info("{}{} start render raw ConfigMap CRDs",
                    LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
            configureRateLimitConfigMap(rawResourceContainer, resourcePacks, plugin);
        }

        return resourcePacks;
    }

    /**
     * ??????????????????????????????????????????ConfigMap??????
     * 1.???????????????????????????
     * 2.???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param plugin ??????????????????????????????????????????
     * @return ??????????????????ConfigMap??????
     */
    private boolean isNeedToRenderConfigMap(GatewayPlugin plugin) {
        return plugin.isRoutePlugin() &&
                (StringUtils.isEmpty(plugin.getPluginType()) || plugin.getPluginType().equals(
                    PluginConstant.RATE_LIMIT_PLUGIN_TYPE));
    }

    /**
     * ???????????????GatewayPlugin CRD??????
     *
     * @param rawResourceContainer ???????????????????????????
     * @param plugin               ????????????
     * @return k8s????????????
     */
    private List<K8sResourcePack> configureGatewayPlugin(RawResourceContainer rawResourceContainer,
                                                         GatewayPlugin plugin) {
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        // ??????????????????GatewayPlugin???CRD???
        List<String> rawGatewayPlugins = renderTwiceModelProcessor.process(gatewayPlugin, plugin,
                new GatewayPluginDataHandler(
                        rawResourceContainer.getVirtualServices(), globalConfig.getResourceNamespace()));

        // ???????????????????????????????????????GatewayPlugin??????????????????
        DynamicGatewayPluginSupplier dynamicGatewayPluginSupplier =
                new DynamicGatewayPluginSupplier(plugin.getGateway(), plugin.getRouteId(), "%s-%s");

        resourcePacks.addAll(generateK8sPack(rawGatewayPlugins,
                null,
                new GatewayPluginNormalSubtracter(),
                new DynamicResourceGenerator(dynamicGatewayPluginSupplier)));
        logger.info("{}{} raw GatewayPlugin CRDs added ok",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);

        return resourcePacks;
    }

    /**
     * ?????????????????????ConfigMap CRD??????
     *
     * @param rawResourceContainer ???????????????????????????
     * @param resourcePacks        k8s????????????
     * @param plugin               ????????????
     */
    private void configureRateLimitConfigMap(RawResourceContainer rawResourceContainer,
                                             List<K8sResourcePack> resourcePacks,
                                             GatewayPlugin plugin) {
        // ???????????????????????????configMap??????
        List<String> rawConfigMaps = neverNullRenderTwiceProcessor.process(rateLimitConfigMap, plugin,
                new GatewayPluginConfigMapDataHandler(
                        rawResourceContainer.getSharedConfigs(), rateLimitConfigMapName, rateLimitNamespace));
        // ??????????????????configMap??????
        resourcePacks.addAll(generateK8sPack(rawConfigMaps,
                new GatewayRateLimitConfigMapMerger(),
                new GatewayRateLimitConfigMapSubtracter(plugin.getGateway(), plugin.getRouteId()),
                new EmptyResourceGenerator(new EmptyConfigMap(rateLimitConfigMapName, rateLimitNamespace))));
        logger.info("{}{} raw ConfigMap CRDs added ok", LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
    }

    public List<K8sResourcePack> translate(Service service) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> destinations = defaultModelProcessor.process(serviceDestinationRule, service, new PortalDestinationRuleServiceDataHandler());
        resources.addAll(generateK8sPack(destinations));
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            List<String> serviceEntries = defaultModelProcessor.process(serviceServiceEntry, service, new PortalServiceEntryServiceDataHandler());
            resources.addAll(generateK8sPack(serviceEntries));
        }
        return resources;
    }

    /**
     * ???gateway?????????istio???????????????
     *
     * @param istioGateway
     * @return
     */
    public List<K8sResourcePack> translate(IstioGateway istioGateway) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> rawGateways = defaultModelProcessor.process(apiGateway, istioGateway, new PortalGatewayDataHandler(enableHttp10));
        resources.addAll(generateK8sPack(rawGateways));
        return resources;
    }

    public List<K8sResourcePack> translate(PluginOrder po) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> pluginManagers = defaultModelProcessor.process(pluginManager, po, new PluginOrderDataHandler());
        resources.addAll(generateK8sPack(pluginManagers));
        return resources;
    }

    private List<FragmentHolder> renderPlugins(List<String> pluginList) {

        if (CollectionUtils.isEmpty(pluginList)) {
            return Collections.emptyList();
        }

        List<String> plugins = pluginList.stream()
                .filter(p -> !StringUtils.isEmpty(p))
                .collect(Collectors.toList());

        return pluginService.processPlugin(plugins, new ServiceInfo());
    }

    private HasMetadata adjust(HasMetadata rawVs) {
        if (VIRTUAL_SERVICE.equalsIgnoreCase(rawVs.getKind())) {
            // VS?????????????????????VS????????????????????????????????????Route??????
            VirtualService originalVs = (VirtualService) rawVs;
            VirtualServiceOuterClass.VirtualService originalSpec = originalVs.getSpec();
            List<VirtualServiceOuterClass.HTTPRoute> originalRoutes = Optional.ofNullable(originalSpec.getHttpList()).orElse(new ArrayList<>());
            // ??????RouteList????????????UnmodifiableList???????????????????????????
            List<VirtualServiceOuterClass.HTTPRoute> resRoutes = new ArrayList<>(originalRoutes.size());
            for (VirtualServiceOuterClass.HTTPRoute route : originalRoutes) {
                if (route.hasReturn() || route.hasRedirect()) {
                    resRoutes.add(route.toBuilder().clearRoute().build());
                } else {
                    resRoutes.add(route);
                }
            }
            // proto???????????????Java??????????????????????????????????????????????????????"Builder??????"??????????????????????????????????????????
            VirtualService resVs = new VirtualService();
            resVs.setKind(originalVs.getKind());
            resVs.setApiVersion(originalVs.getApiVersion());
            resVs.setMetadata(originalVs.getMetadata());
            resVs.setSpec(VirtualServiceOuterClass.VirtualService.newBuilder()
                    // HTTP???????????????????????????
                    .addAllHttp(resRoutes)
                    .addAllGateways(originalSpec.getGatewaysList())
                    .addAllHosts(originalSpec.getHostsList())
                    .addAllDubbo(originalSpec.getDubboList())
                    .addAllExportTo(originalSpec.getExportToList())
                    .addAllVirtualCluster(originalSpec.getVirtualClusterList())
                    .addAllTcp(originalSpec.getTcpList())
                    .addAllThrift(originalSpec.getThriftList())
                    .addAllTls(originalSpec.getTlsList())
                    .setPriority(originalSpec.getPriority())
                    .build());
            return resVs;
        } else {
            // ???VS??????????????????
            return rawVs;
        }
    }
}
