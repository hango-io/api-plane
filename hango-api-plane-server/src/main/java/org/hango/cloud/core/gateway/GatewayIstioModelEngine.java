package org.hango.cloud.core.gateway;

import io.fabric8.kubernetes.api.model.HasMetadata;
import istio.networking.v1alpha3.VirtualServiceOuterClass;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.IstioModelEngine;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.gateway.handler.*;
import org.hango.cloud.core.gateway.processor.DefaultModelProcessor;
import org.hango.cloud.core.gateway.processor.NeverReturnNullModelProcessor;
import org.hango.cloud.core.gateway.processor.RenderTwiceModelProcessor;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.k8s.empty.DynamicGatewayPluginSupplier;
import org.hango.cloud.core.k8s.operator.IntegratedResourceOperator;
import org.hango.cloud.core.k8s.subtracter.GatewayPluginNormalSubtracter;
import org.hango.cloud.core.k8s.subtracter.GatewayVirtualServiceSubtracter;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateTranslator;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.k8s.K8sTypes.VirtualService;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.GrpcEnvoyFilterDTO;
import org.hango.cloud.meta.dto.IpSourceEnvoyFilterDTO;
import org.hango.cloud.service.PluginService;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.HandlerUtil;
import org.hango.cloud.util.constant.LogConstant;
import org.hango.cloud.util.function.Subtracter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    private static final String API_GATEWAY = "gateway/api/gateway";
    private static final String API_VIRTUAL_SERVICE = "gateway/api/virtualService";

    private static final String SERVICE_DESTINATION_RULE = "gateway/service/destinationRule";
    private static final String PLUGIN_MANAGER = "gateway/pluginManager";
    private static final String SERVICE_SERVICE_ENTRY = "gateway/service/serviceEntry";
    private static final String GATEWAY_PLUGIN = "gateway/gatewayPlugin";
    private static final String SMART_LIMITER = "gateway/smartLimiter";
    private static final String ENVOY_FILTER = "gateway/envoyFilter";
    private static final String GRPC_CONFIG_PATCH = "gateway/grpcConfigPatch";
    private static final String IP_SOURCE_CONFIG_PATCH = "gateway/ipSourceConfigPatch";
    private static final String VIRTUAL_SERVICE = "VirtualService";
    private static final String SECRET = "gateway/secret";

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
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        if (NumberUtils.INTEGER_ZERO.equals(api.getCustomDefaultRespCode())){
            api.setCustomDefaultRespCode(globalConfig.getCustomDefaultRespCode());
        }
        List<String> rawVirtualServices = renderTwiceModelProcessor
                .process(API_VIRTUAL_SERVICE, api,
                        new PortalVirtualServiceAPIDataHandler(
                                defaultModelProcessor, vsFragments, simple));

        logger.info("{}{} start to generate and add k8s resource",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.ROUTE_LOG_NOTE);
        resourcePacks.addAll(generateK8sPack(rawVirtualServices,
                new GatewayVirtualServiceSubtracter(HandlerUtil.buildVirtualServiceName(api.getName(), api.getProjectId(), api.getGateways().get(0))),
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
        // 插件渲染网关插件CR资源
        logger.info("{}{} start render raw Plugin CRs",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);

        // 渲染EnvoyPlugin和SmartPlugin资源
        List<K8sResourcePack> envoyPlugins = configureEnvoyPlugin(rawResourceContainer, plugin);
        List<K8sResourcePack> smartLimiters = configureSmartLimiter(rawResourceContainer, plugin);

        // 聚合插件CR资源
        return Stream.of(envoyPlugins, smartLimiters)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 插件转换为SmartLimiter CRD资源
     *
     * @param rawResourceContainer 存放资源的容器对象
     * @param plugin               插件对象
     * @return k8s资源集合
     */
    private List<K8sResourcePack> configureSmartLimiter(RawResourceContainer rawResourceContainer,
                                                        GatewayPlugin plugin) {
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        // 将插件配置转换为SmartLimiters
        List<String> rawSmartLimiters = defaultModelProcessor.process(SMART_LIMITER, plugin,
                new SmartLimiterDataHandler(rawResourceContainer.getSmartLimiters(), globalConfig.getResourceNamespace()));

        resourcePacks.addAll(generateK8sPack(rawSmartLimiters));
        logger.info("{}{} raw SmartLimiter CRs added ok",
                LogConstant.TRANSLATE_LOG_NOTE, LogConstant.PLUGIN_LOG_NOTE);

        return resourcePacks;
    }

    /**
     * 插件转换为GatewayPlugin CRD资源
     *
     * @param rawResourceContainer 存放资源的容器对象
     * @param plugin               插件对象
     * @return k8s资源集合
     */
    private List<K8sResourcePack> configureEnvoyPlugin(RawResourceContainer rawResourceContainer,
                                                       GatewayPlugin plugin) {
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        // 插件配置放在GatewayPlugin的CRD上
        List<String> rawGatewayPlugins = renderTwiceModelProcessor.process(GATEWAY_PLUGIN, plugin,
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

    public List<K8sResourcePack> translate(Service service) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> destinations = defaultModelProcessor.process(SERVICE_DESTINATION_RULE, service, new PortalDestinationRuleServiceDataHandler());
        resources.addAll(generateK8sPack(destinations));
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            List<String> serviceEntries = defaultModelProcessor.process(SERVICE_SERVICE_ENTRY, service, new PortalServiceEntryServiceDataHandler());
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
        List<String> rawGateways = defaultModelProcessor.process(API_GATEWAY, istioGateway, new PortalGatewayDataHandler(enableHttp10, globalConfig.getResourceNamespace()));
        resources.addAll(generateK8sPack(rawGateways));
        return resources;
    }

    public List<K8sResourcePack> translate(PluginOrder po) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> pluginManagers = defaultModelProcessor.process(PLUGIN_MANAGER, po, new PluginOrderDataHandler());
        resources.addAll(generateK8sPack(pluginManagers));
        return resources;
    }

    public List<K8sResourcePack> translate(Secret secret) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> secrets = defaultModelProcessor.process(SECRET, secret, new SecretDataHandler());
        resources.addAll(generateK8sPack(secrets));
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
                    .setPriority(originalSpec.getPriority())
                    .build());
            return resVs;
        } else {
            // 非VS资源下不处理
            return rawVs;
        }
    }

    public List<K8sResourcePack> translate(EnvoyFilterOrder efo) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> pluginManagers = defaultModelProcessor.process(ENVOY_FILTER, efo, new EnvoyFilterOrderDataHandler());
        resources.addAll(generateK8sPack(pluginManagers,subtract()));
        return resources;
    }

    Subtracter<K8sTypes.EnvoyFilter> subtract() {
        return envoyFilter -> {
            envoyFilter.setApiVersion(null);
            envoyFilter.setSpec(null);
            return envoyFilter;
        };
    }

    public List<String> generateEnvoyConfigObjectPatch(GrpcEnvoyFilterDTO grpcEnvoyFilterDto) {
        return defaultModelProcessor.process(GRPC_CONFIG_PATCH, new GrpcEnvoyFilterDataHandler().handle(grpcEnvoyFilterDto));
    }

    public List<String> generateEnvoyConfigObjectPatch(IpSourceEnvoyFilterDTO ipSourceEnvoyFilterDTO) {
        return defaultModelProcessor.process(IP_SOURCE_CONFIG_PATCH, new IpSourceEnvoyFilterDataHandler().handle(ipSourceEnvoyFilterDTO));
    }
}
