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
     * 将api转换为istio对应的规则
     *
     * @param api    路由信息
     * @param simple 是否为简单模式，部分字段不渲染，主要用于删除
     * @return K8s资源集合
     */
    public List<K8sResourcePack> translate(API api, boolean simple) {
        logger.info("{}{} start translate k8s resource", LogConstant.TRANSLATE_LOG_NOTE, LogConstant.ROUTE_LOG_NOTE);
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        api.setNamespace(globalConfig.getResourceNamespace());
        List<FragmentWrapper> vsFragments = new ArrayList<>();
        // 渲染VS上的插件片段（当前仅有Match插件）
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
     * 转换GatewayPlugin数据为CRD资源
     *
     * @param plugin 网关插件实例（内含插件配置）
     * @return k8s资源集合
     */
    public List<K8sResourcePack> translate(GatewayPlugin plugin) {
        logger.info("{}{} start translate k8s resource", LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);

        // 打印插件配置信息
        plugin.showPluginConfigsInLog(logger);

        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(plugin.getPlugins()));

        logger.info("{}{} render plugins ok, start to generate and add k8s resource",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
        return generateAndAddK8sResource(rawResourceContainer, plugin);
    }

    /**
     * 生成k8s资源并将其合并为k8s资源集合
     *
     * @param rawResourceContainer k8s资源片段存放容器实例
     * @param plugin               网关插件实例（内含插件配置）
     * @return k8s资源集合
     */
    private List<K8sResourcePack> generateAndAddK8sResource(RawResourceContainer rawResourceContainer,
                                                            GatewayPlugin plugin) {
        // 插件渲染GatewayPlugin资源
        logger.info("{}{} start render raw GatewayPlugin CRDs",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
        List<K8sResourcePack> resourcePacks = configureGatewayPlugin(rawResourceContainer, plugin);

        // 路由级别的限流插件要渲染ConfigMap资源
        if (isNeedToRenderConfigMap(plugin)) {
            logger.info("{}{} start render raw ConfigMap CRDs",
                    LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);
            configureRateLimitConfigMap(rawResourceContainer, resourcePacks, plugin);
        }

        return resourcePacks;
    }

    /**
     * 插件流程只有两个场景需要渲染ConfigMap资源
     * 1.路由级别的限流插件
     * 2.没有传插件类型，即批量操作的场景，此时可能会有限流插件，因此需要无差别渲染（匹配路由启用、禁用、下线场景）
     * 注：限流插件只有路由级别，因此路由插件是先决条件
     *
     * @param plugin 网关插件实例（内含插件配置）
     * @return 是否需要渲染ConfigMap资源
     */
    private boolean isNeedToRenderConfigMap(GatewayPlugin plugin) {
        return plugin.isRoutePlugin() &&
                (StringUtils.isEmpty(plugin.getPluginType()) || plugin.getPluginType().equals(
                    PluginConstant.RATE_LIMIT_PLUGIN_TYPE));
    }

    /**
     * 插件转换为GatewayPlugin CRD资源
     *
     * @param rawResourceContainer 存放资源的容器对象
     * @param plugin               插件对象
     * @return k8s资源集合
     */
    private List<K8sResourcePack> configureGatewayPlugin(RawResourceContainer rawResourceContainer,
                                                         GatewayPlugin plugin) {
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        // 插件配置放在GatewayPlugin的CRD上
        List<String> rawGatewayPlugins = renderTwiceModelProcessor.process(gatewayPlugin, plugin,
                new GatewayPluginDataHandler(
                        rawResourceContainer.getVirtualServices(), globalConfig.getResourceNamespace()));

        // 当插件传入为空时，生成空的GatewayPlugin，删除时使用
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
     * 限流插件转换为ConfigMap CRD资源
     *
     * @param rawResourceContainer 存放资源的容器对象
     * @param resourcePacks        k8s资源集合
     * @param plugin               插件对象
     */
    private void configureRateLimitConfigMap(RawResourceContainer rawResourceContainer,
                                             List<K8sResourcePack> resourcePacks,
                                             GatewayPlugin plugin) {
        // 限流插件需要额外的configMap配置
        List<String> rawConfigMaps = neverNullRenderTwiceProcessor.process(rateLimitConfigMap, plugin,
                new GatewayPluginConfigMapDataHandler(
                        rawResourceContainer.getSharedConfigs(), rateLimitConfigMapName, rateLimitNamespace));
        // 加入限流插件configMap配置
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
     * 将gateway转换为istio对应的规则
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
            // VS需要特殊处理，VS中有中断或重定向插件则将Route清空
            VirtualService originalVs = (VirtualService) rawVs;
            VirtualServiceOuterClass.VirtualService originalSpec = originalVs.getSpec();
            List<VirtualServiceOuterClass.HTTPRoute> originalRoutes = Optional.ofNullable(originalSpec.getHttpList()).orElse(new ArrayList<>());
            // 修改RouteList，此处为UnmodifiableList，需要新建集合对象
            List<VirtualServiceOuterClass.HTTPRoute> resRoutes = new ArrayList<>(originalRoutes.size());
            for (VirtualServiceOuterClass.HTTPRoute route : originalRoutes) {
                if (route.hasReturn() || route.hasRedirect()) {
                    resRoutes.add(route.toBuilder().clearRoute().build());
                } else {
                    resRoutes.add(route);
                }
            }
            // proto文件生成的Java对象没有直接修改属性的方法，只能通过"Builder模式"，因此需要创建新资源修改属性
            VirtualService resVs = new VirtualService();
            resVs.setKind(originalVs.getKind());
            resVs.setApiVersion(originalVs.getApiVersion());
            resVs.setMetadata(originalVs.getMetadata());
            resVs.setSpec(VirtualServiceOuterClass.VirtualService.newBuilder()
                    // HTTP字段用处理过的替换
                    .addAllHttp(resRoutes)
                    .addAllGateways(originalSpec.getGatewaysList())
                    .addAllHosts(originalSpec.getHostsList())
                    .addAllDubbo(originalSpec.getDubboList())
                    .addAllExportTo(originalSpec.getExportToList())
                    .addAllVirtualCluster(originalSpec.getVirtualClusterList())
                    .addAllTcp(originalSpec.getTcpList())
                    .addAllThrift(originalSpec.getThriftList())
                    .addAllTls(originalSpec.getTlsList())
                    .build());
            return resVs;
        } else {
            // 非VS资源下不处理
            return rawVs;
        }
    }
}
