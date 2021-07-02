package org.hango.cloud.core.gateway;

import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.IstioModelEngine;
import org.hango.cloud.core.gateway.handler.*;
import org.hango.cloud.core.gateway.processor.DefaultModelProcessor;
import org.hango.cloud.core.gateway.processor.RenderTwiceModelProcessor;
import org.hango.cloud.core.k8s.empty.DynamicGatewayPluginSupplier;
import org.hango.cloud.core.k8s.operator.IntegratedResourceOperator;
import org.hango.cloud.core.k8s.subtracter.GatewayPluginNormalSubtracter;
import org.hango.cloud.core.k8s.subtracter.GatewayVirtualServiceSubtracter;
import org.hango.cloud.core.template.TemplateTranslator;
import org.hango.cloud.meta.*;
import org.hango.cloud.util.Const;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.gateway.processor.NeverReturnNullModelProcessor;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.service.PluginService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
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

    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String apiDestinationRule = "gateway/api/destinationRule";
    private static final String apiSharedConfigConfigMap = "gateway/api/sharedConfigConfigMap";

    private static final String serviceDestinationRule = "gateway/service/destinationRule";
    private static final String pluginManager = "gateway/pluginManager";
    private static final String serviceServiceEntry = "gateway/service/serviceEntry";
    private static final String globalGatewayPlugin = "gateway/globalGatewayPlugin";
    private static final String gatewayPlugin = "gateway/gatewayPlugin";

    public List<K8sResourcePack> translate(API api) {
        return translate(api, false);
    }

    /**
     * 将api转换为istio对应的规则
     *
     * @param api
     * @param simple 是否为简单模式，部分字段不渲染，主要用于删除
     * @return List<K8sResourcePack> 对应K8s资源
     */
    public List<K8sResourcePack> translate(API api, boolean simple) {

        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        BaseVirtualServiceAPIDataHandler apiHandler = new PortalVirtualServiceAPIDataHandler(defaultModelProcessor);

        String matchYaml = apiHandler.produceMatch(apiHandler.handleApi(api));
        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(api, matchYaml));

        BaseVirtualServiceAPIDataHandler vsHandler = new PortalVirtualServiceAPIDataHandler(
                    defaultModelProcessor, rawResourceContainer.getVirtualServices(), simple);

        List<String> rawVirtualServices = renderTwiceModelProcessor.process(apiVirtualService, api, vsHandler);
        // vs上的插件转移到gatewayplugin上
        List<String> rawGatewayPlugins = neverNullRenderTwiceProcessor.process(gatewayPlugin, api,
                new ApiGatewayPluginDataHandler(rawResourceContainer.getVirtualServices(),
                                                globalConfig.getResourceNamespace(), globalConfig.getLdsPort()));

        resourcePacks.addAll(generateK8sPack(rawVirtualServices, new GatewayVirtualServiceSubtracter(vsHandler.getApiName(api)), r -> r, this::adjust));
        //当插件传入为空时，生成空的gatewayplugin，删除时使用
        DynamicGatewayPluginSupplier dynamicGatewayPluginSupplier = new DynamicGatewayPluginSupplier(api.getGateways(), api.getName(), "%s-%s");
        resourcePacks.addAll(generateK8sPack(rawGatewayPlugins,
                null,
                new GatewayPluginNormalSubtracter(),
                new DynamicResourceGenerator(dynamicGatewayPluginSupplier)));

        return resourcePacks;
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

    public List<K8sResourcePack> translate(GlobalPlugin gp) {

        List<K8sResourcePack> resources = new ArrayList<>();
        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        List<FragmentHolder> plugins = pluginService.processPlugin(gp.getPlugins(), new ServiceInfo());
        rawResourceContainer.add(plugins);
        List<Gateway> gateways = resourceManager.getGatewayList();

        List<String> rawGatewayPlugins = defaultModelProcessor.process(globalGatewayPlugin, gp,
                new GatewayPluginDataHandler(rawResourceContainer.getGatewayPlugins(), gateways, globalConfig.getResourceNamespace()));
        resources.addAll(generateK8sPack(rawGatewayPlugins));
        return resources;
    }

    private List<FragmentHolder> renderPlugins(API api, String matchYaml) {

        if (CollectionUtils.isEmpty(api.getPlugins())) return Collections.emptyList();
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setMatchYaml(matchYaml);

        List<String> plugins = api.getPlugins().stream()
                .filter(p -> !StringUtils.isEmpty(p))
                .collect(Collectors.toList());
        api.setPlugins(plugins);

        return pluginService.processPlugin(plugins, serviceInfo);
    }

    private HasMetadata adjust(HasMetadata rawVs) {
        if ("VirtualService".equalsIgnoreCase(rawVs.getKind())) {
            VirtualService vs = (VirtualService) rawVs;
            List<HTTPRoute> routes = Optional.ofNullable(vs.getSpec().getHttp()).orElse(new ArrayList<>());
            routes.forEach(route -> {
//                if (Objects.nonNull(route.getReturn())) route.setRoute(null);
                if (Objects.nonNull(route.getRedirect())) route.setRoute(null);
                if (Objects.nonNull(route.getRedirect())) route.setFault(null);
            });
        }
        return rawVs;
    }

}
