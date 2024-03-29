package org.hango.cloud.service.impl;

import freemarker.template.Configuration;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.*;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.Port;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.envoy.EnvoyHttpClient;
import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.istio.PilotHttpClient;
import org.hango.cloud.core.k8s.K8sClient;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.meta.Secret;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.meta.enums.PluginMappingEnum;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.service.PluginService;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.TelnetUtil;
import org.hango.cloud.util.Trans;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GatewayServiceImpl implements GatewayService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceImpl.class);

    private static final String SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN = "ROUND_ROBIN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN = "LEAST_CONN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_RANDOM = "RANDOM";
    private static final String DUBBO_TELNET_COMMAND_TEMPLATE = "ls -l %s";
    private static final String DUBBO_TELNET_COMMAND_END_PATTERN = "dubbo>";
    private static final Pattern DUBBO_INFO_PATTRERN = Pattern.compile("^(\\S*) (\\S*)\\((\\S*)\\)$");
    private static final Pattern DUBBO_TELNET_RETURN_PATTERN = Pattern.compile("[\\s\\S]*?\\(as (provider|consumer)\\):");
    public static final String GW_CLUSTER = "gw_cluster";

    public static final String HOST_NETWORK = "HostNetwork";

    public static final String CLUSTER_IP = "ClusterIP";

    public static final String NODE_PORT = "NodePort";

    public static final String LOAD_BALANCER = "LoadBalancer";

    @Value(value = "${gatewayName:gateway-proxy}")
    private String gatewayName;

    private ResourceManager resourceManager;

    private GatewayConfigManager configManager;

    private GlobalConfig globalConfig;

    @Autowired
    private Configuration configuration;

    @Autowired
    private PilotHttpClient pilotHttpClient;

    @Autowired
    private EnvoyHttpClient envoyHttpClient;

    @Autowired
    private K8sClient k8sClient;

    @Autowired
    private PluginService pluginService;


    public GatewayServiceImpl(ResourceManager resourceManager, GatewayConfigManager configManager, GlobalConfig globalConfig) {
        this.resourceManager = resourceManager;
        this.configManager = configManager;
        this.globalConfig = globalConfig;
    }

    @Override
    public void updateAPI(PortalAPIDTO api) {
        configManager.updateConfig(Trans.portalAPI2API(api));
    }

    /**
     * 更新路由级插件，下发envoyplugin资源
     * @param plugin
     */
    @Override
    public void updateGatewayPlugin(GatewayPluginDTO plugin) {
        configManager.updateConfig(Trans.pluginDTOToPlugin(plugin));
    }


    @Override
    public void updateBasePlugin(BasePluginDTO plugin) {
        configManager.updateConfig(Trans.trans(plugin));
    }


    @Override
    public void deleteAPI(PortalAPIDeleteDTO api) {
        configManager.deleteConfig(Trans.portalDeleteAPI2API(api));
    }

    @Override
    public void updateService(PortalServiceDTO service) {
        configManager.updateConfig(Trans.portalService2Service(service));
    }

    /**
     * 校验服务和版本负载均衡策略 & 连接池 且 根据Type字段将冗余字段置空不处理
     *
     * @param service
     * @return
     */
    @Override
    public ErrorCode checkUpdateService(PortalServiceDTO service) {
        PortalTrafficPolicyDTO envoyServiceTrafficPolicyDto = service.getTrafficPolicy();
        ErrorCode errorCode = checkTrafficPolicy(envoyServiceTrafficPolicyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return errorCode;
        }

        List<ServiceSubsetDTO> envoySubsetDtoList = service.getSubsets();
        if (envoySubsetDtoList != null) {
            for (ServiceSubsetDTO envoySubsetDto : envoySubsetDtoList) {
                errorCode = checkTrafficPolicy(envoySubsetDto.getTrafficPolicy());
                if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
                    return errorCode;
                }
            }
        }
        return ApiPlaneErrorCode.Success;

    }

    /**
     * 校验负载均衡策略 & 连接池 且 根据Type字段将冗余字段置空不处理
     *
     * @param portalTrafficPolicyDTO
     * @return
     */
    private ErrorCode checkTrafficPolicy(PortalTrafficPolicyDTO portalTrafficPolicyDTO) {
        if (portalTrafficPolicyDTO == null) {
            return ApiPlaneErrorCode.Success;
        }

        PortalLoadBalancerDTO envoyServiceLoadBalancerDto = portalTrafficPolicyDTO.getLoadBalancer();
        if (envoyServiceLoadBalancerDto != null) {
            //Simple类型，包含ROUND_ROBIN|LEAST_CONN|RANDOM
            final List<String> simpleList = new ArrayList<>();
            simpleList.add(SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN);
            simpleList.add(SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN);
            simpleList.add(SERVICE_LOADBALANCER_SIMPLE_RANDOM);
            if (StringUtils.isNotBlank(envoyServiceLoadBalancerDto.getSimple()) &&
                    !simpleList.contains(envoyServiceLoadBalancerDto.getSimple())) {
                return ApiPlaneErrorCode.InvalidSimpleLoadBanlanceType;
            }

            //一致性哈希
            PortalLoadBalancerDTO.ConsistentHashDTO envoyServiceConsistentHashDto = envoyServiceLoadBalancerDto.getConsistentHashDTO();
            if (envoyServiceConsistentHashDto != null) {
                PortalLoadBalancerDTO.ConsistentHashDTO.HttpCookieDTO envoyServiceConsistentHashCookieDto =
                        envoyServiceConsistentHashDto.getHttpCookie();
                if (envoyServiceConsistentHashCookieDto != null) {
                    String name = envoyServiceConsistentHashCookieDto.getName();
                    if (StringUtils.isBlank(name)) {
                        return ApiPlaneErrorCode.InvalidConsistentHashHttpCookieName;
                    }
                    Integer ttl = envoyServiceConsistentHashCookieDto.getTtl();
                    if (ttl == null || ttl < 0) {
                        return ApiPlaneErrorCode.InvalidConsistentHashHttpCookieTtl;
                    }
                }
            }
            // 服务预热时间窗校验，为空则代表不开启功能；时间窗仅支持[1, 3600]区间配置
            if (envoyServiceLoadBalancerDto.getSlowStartWindow() != null &&
                    (envoyServiceLoadBalancerDto.getSlowStartWindow() > 3600 || envoyServiceLoadBalancerDto.getSlowStartWindow() < 1)) {
                return ApiPlaneErrorCode.InvalidSlowStartWindow;
            }
        }
        PortalServiceConnectionPoolDTO envoyServiceConnectionPoolDto = portalTrafficPolicyDTO.getConnectionPool();
        if (envoyServiceConnectionPoolDto != null) {
            PortalServiceConnectionPoolDTO.PortalServiceHttpConnectionPoolDTO envoyServiceHttpConnectionPoolDto = envoyServiceConnectionPoolDto.getHttp();
            PortalServiceConnectionPoolDTO.PortalServiceTcpConnectionPoolDTO envoyServiceTcpConnectionPoolDto = envoyServiceConnectionPoolDto.getTcp();
            if (envoyServiceHttpConnectionPoolDto != null) {
                Integer http1MaxPendingRequests = envoyServiceHttpConnectionPoolDto.getHttp1MaxPendingRequests();
                Integer http2MaxRequests = envoyServiceHttpConnectionPoolDto.getHttp2MaxRequests();
                Integer idleTimeout = envoyServiceHttpConnectionPoolDto.getIdleTimeout();
                Integer maxRequestsPerConnection = envoyServiceHttpConnectionPoolDto.getMaxRequestsPerConnection();
                if (http1MaxPendingRequests < 0) {
                    return ApiPlaneErrorCode.InvalidHttp1MaxPendingRequests;
                }
                if (http2MaxRequests < 0) {
                    return ApiPlaneErrorCode.InvalidHttp2MaxRequests;
                }
                if (idleTimeout < 0) {
                    return ApiPlaneErrorCode.InvalidIdleTimeout;
                }
                if (maxRequestsPerConnection < 0) {
                    return ApiPlaneErrorCode.InvalidMaxRequestsPerConnection;
                }
            }
            if (envoyServiceTcpConnectionPoolDto != null) {
                Integer maxConnections = envoyServiceTcpConnectionPoolDto.getMaxConnections();
                Integer connectTimeout = envoyServiceTcpConnectionPoolDto.getConnectTimeout();
                if (maxConnections < 0) {
                    return ApiPlaneErrorCode.InvalidMaxConnections;
                }
                if (connectTimeout < 0) {
                    return ApiPlaneErrorCode.InvalidConnectTimeout;
                }
            }
        }
        return ApiPlaneErrorCode.Success;
    }


    @Override
    public void deleteService(PortalServiceDTO service) {
        configManager.deleteConfig(Trans.portalService2Service(service));
    }


    @Override
    public void updateSecret(PortalSecretDTO portalSecretDTO) {
        Secret secret = Trans.secretDTO2Secret(portalSecretDTO);
        configManager.updateConfig(secret);
    }

    @Override
    public void deleteSecret(PortalSecretDTO portalSecretDTO) {
        Secret secret = Trans.secretDTO2Secret(portalSecretDTO);
        configManager.updateConfig(secret);
    }


    @Override
    public void updateGrpcEnvoyFilter(GrpcEnvoyFilterDTO grpcEnvoyFilterDto) {
        EnvoyFilterOrder envoyFilterOrder = Trans.transEnvoyFilter(grpcEnvoyFilterDto);
        envoyFilterOrder.setConfigPatches(configManager.generateEnvoyConfigObjectPatch(grpcEnvoyFilterDto));
        configManager.updateConfig(envoyFilterOrder);
    }

    @Override
    public void updateIpSourceEnvoyFilter(IpSourceEnvoyFilterDTO ipSourceEnvoyFilterDto) {
        EnvoyFilterOrder envoyFilterOrder = Trans.transEnvoyFilter(ipSourceEnvoyFilterDto);
        envoyFilterOrder.setConfigPatches(configManager.generateEnvoyConfigObjectPatch(ipSourceEnvoyFilterDto));
        configManager.updateConfig(envoyFilterOrder);
    }

    @Override
    public void deleteEnvoyFilter(EnvoyFilterDTO envoyFilterDto) {
        configManager.deleteConfig(Trans.transEnvoyFilter(envoyFilterDto));
    }

    @Override
    public List<String> getServiceList() {
        return resourceManager.getServiceList();
    }

    @Override
    public List<String> getRegistryList() {
        return globalConfig.getRegistryList();
    }

    @Override
    public List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId, Map<String, String> filters) {
        logger.info("[get service] start getServiceAndPortList, name: {}, type: {}, registryId: {}", name, type, registryId);
        String pattern = ".*";
        if (!StringUtils.isEmpty(name)) {
            pattern = "^" + pattern + name + pattern + "$";
        }
        final Pattern fPattern = Pattern.compile(pattern);
        return resourceManager.getServiceAndPortList(filters).stream()
                .filter(sap -> fPattern.matcher(sap.getName()).find())
                .filter(sap -> matchType(type, sap.getName(), registryId))
                .map(sap -> {
                    ServiceAndPortDTO dto = new ServiceAndPortDTO();
                    dto.setName(sap.getName());
                    dto.setPort(sap.getPort());
                    return dto;
                }).collect(Collectors.toList());
    }


    @Override
    public List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway) {
        return resourceManager.getServiceHealthList(host, subsets, gateway);
    }

    private boolean matchType(String type, String name, String registryId) {
        if (StringUtils.isEmpty(type)) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_CONSUL) && StringUtils.isEmpty(registryId) && Pattern.compile(".*\\.consul\\.(.*?)").matcher(name).find())
            return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_CONSUL) && name.endsWith(String.format(".consul.%s", registryId)))
            return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_K8S) && name.endsWith(".svc.cluster.local")) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_DUBBO) && name.endsWith(".dubbo")) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_EUREKA) && name.endsWith(".eureka")) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_NACOS) && name.endsWith(".nacos")) return true;
        return false;
    }


    @Override
    public List<DubboMetaDto> getDubboMeta(String igv) {
        //获取dubbo实例
        List<Endpoint> endpoints = pilotHttpClient.getDubboEndpoints(igv);
        logger.info("dubbo endpoint filter result count is {}", endpoints.size());
        if (CollectionUtils.isEmpty(endpoints)){
            return new ArrayList<>();
        }
        //随机选择一个实例
        Random random = new Random();
        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));
        logger.info("选取的后端节点信息为 {}", ToStringBuilder.reflectionToString(endpoint, ToStringStyle.SHORT_PREFIX_STYLE));
        //dubbo telnet获取method信息
        return handlerInfo(endpoint);
    }


    @Override
    public void updatePluginStatus(PluginStatusDTO pluginStatusDTO) {
        if (!org.springframework.util.StringUtils.hasText(pluginStatusDTO.getPluginManagerName())
                || !org.springframework.util.StringUtils.hasText(pluginStatusDTO.getPluginName())) {
            logger.error("pluginManagerName or pluginName is empty");
            return;
        }
        // 查询plm资源
        PluginOrderDTO pluginManager = getPluginManager(pluginStatusDTO.getPluginManagerName());
        if (CollectionUtils.isEmpty(pluginManager.getPlugins())) {
            logger.error("pluginManagerName is not exist");
            return;
        }
        PluginOrderItemDTO source = pluginManager.getPlugins().stream().filter(plugin -> plugin.getName().equals(pluginStatusDTO.getPluginName())).findFirst().orElse(null);
        if (source == null){
            logger.error("pluginName is not exist");
            return;
        }
        //设置状态
        source.setEnable(pluginStatusDTO.getEnable());
        //更新资源
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginManager);
        configManager.updateConfig(pluginOrder);
    }

    @Override
    public void reloadPluginOrder(PluginOrderDTO pluginManager) {
        //重新load更新资源
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginManager);
        configManager.updateConfig(pluginOrder);
    }

    @Override
    public void publishPluginOrder(PluginOrderDTO pluginOrderDto) {
        PluginOrderDTO pluginOrder = pluginService.getPluginOrderTemplate(pluginOrderDto.getGatewayKind());
        pluginOrder.setName(pluginOrderDto.getName());
        pluginOrder.getPlugins().forEach(o -> o.setPort(pluginOrderDto.getPort()));
        pluginOrder.setGwCluster(pluginOrderDto.getGwCluster());
        configManager.updateConfig(Trans.pluginOrderDTO2PluginOrder(pluginOrder));
    }

    /**
     * 校验相同端口的plm资源中是否已经下发
     */
    @Override
    public boolean pluginOrderPortCheck(PluginOrderDTO pluginOrderDto){
        List<PluginOrderDTO> pluginManagerList = getPluginManagerList(pluginOrderDto.getGwCluster());
        if (CollectionUtils.isEmpty(pluginManagerList)){
            return true;
        }
        for (PluginOrderDTO pluginOrderDTO : pluginManagerList) {
            List<PluginOrderItemDTO> plugins = pluginOrderDTO.getPlugins();
            if (CollectionUtils.isEmpty(plugins)){
                continue;
            }
            if (pluginOrderDto.getName().equals(pluginOrderDTO.getName())){
                continue;
            }
            if (plugins.get(0).getPort().equals(pluginOrderDto.getPort())){
                return false;
            }
        }
        return true;
    }


    @Override
    public void deletePluginOrder(PluginOrderDTO pluginOrderDTO) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDTO);
        configManager.deleteConfig(pluginOrder);
    }

    @Override
    public void updateIstioGateway(PortalIstioGatewayDTO portalGateway) {
        IstioGateway istioGateway = Trans.portalGW2GW(portalGateway);
        configManager.updateConfig(istioGateway);
        //联动创建envoy service端口
        createEnvoyServicePort(istioGateway);
    }

    @Override
    public void deleteIstioGateway(PortalIstioGatewayDTO portalGateway) {
        IstioGateway istioGateway = Trans.portalGW2GW(portalGateway);
        //联动删除envoy service端口
        deleteEnvoyServicePort(istioGateway);
        configManager.deleteConfig(istioGateway);
    }


    /**
     * 发起Dubbo Telnet 请求，并组装数据
     *
     * @param endpoint
     * @return
     */
    private List<DubboMetaDto> handlerInfo(Endpoint endpoint) {
        List<DubboMetaDto> metaList = new ArrayList<>();
        //去除.dubbo 后缀
        String[] igv = splitIgv(endpoint.getHostname());
        String info = TelnetUtil
            .sendCommand(endpoint.getAddress(), NumberUtils.toInt(endpoint.getLabel(Const.DUBBO_TCP_PORT)), globalConfig.getTelnetConnectTimeout(),
                    String.format(DUBBO_TELNET_COMMAND_TEMPLATE, igv), DUBBO_TELNET_COMMAND_END_PATTERN);
        //解析dubbo telnet信息
        String[] methodList = processMessageList(endpoint, info).split("\\r\\n");
        for (String methodInfo : methodList) {
            String methodForTrim = StringUtils.trim(methodInfo);
            Matcher matcher = DUBBO_INFO_PATTRERN.matcher(methodForTrim);
            if (!matcher.matches() || matcher.groupCount() != 3) {
                logger.info("invalid method info , info is {}", methodForTrim);
                continue;
            }
            String returns = matcher.group(1);
            String method = matcher.group(2);
            String params = matcher.group(3);
            DubboMetaDto dubboMetaDto = new DubboMetaDto();
            dubboMetaDto.setApplicationName(endpoint.getLabels().get(Const.DUBBO_APPLICATION));
            dubboMetaDto.setInterfaceName(igv[0]);
            dubboMetaDto.setGroup(igv[1]);
            dubboMetaDto.setVersion(igv[2]);
            dubboMetaDto.setProtocolVersion(endpoint.getLabels().get(Const.PROTOCOL_DUBBO));

            dubboMetaDto.setReturns(returns);
            dubboMetaDto.setMethod(method);
            dubboMetaDto.setParams(Arrays.asList(StringUtils.splitPreserveAllTokens(params, ",")));
            metaList.add(dubboMetaDto);
        }
        return metaList;
    }

    /**
     * 处理dubbo telnet 返回数据
     * <p>
     * Dubbo 2.5.x ~ 2.6.x 版本 dubbo telnet 时 仅显示本 Interface 内的方法
     * {@see https://github.com/apache/dubbo/blob/2.5.x/dubbo-rpc/dubbo-rpc-default/src/main/java/com/alibaba/dubbo/rpc/protocol/dubbo/telnet/ListTelnetHandler.java}
     * {@see https://github.com/apache/dubbo/blob/2.6.x/dubbo-rpc/dubbo-rpc-dubbo/src/main/java/com/alibaba/dubbo/rpc/protocol/dubbo/telnet/ListTelnetHandler.java}
     * 具体格式为:
     * dubbo>ls -l xxxService
     * xxMethod(param1, param2)
     * xxxMethod(param3, param4)
     * <p>
     * Dubbo 2.7.x 版本 dubbo telnet 时， 会显示PROVIDER 侧 及 CONSUMER 侧信息
     * {@see https://github.com/apache/dubbo/blob/dubbo-2.7.14/dubbo-plugin/dubbo-qos/src/main/java/org/apache/dubbo/qos/legacy/ListTelnetHandler.java}
     * 具体格式为:
     * dubbo>ls -l xxxService
     * xxxGroup/xxxService:xxxVersion (as provider):
     * xxMethod(param1, param2)
     * xxxMethod(param3, param4)
     * xxxGroup/xxxService:xxxVersion (as consumer):
     * xxMethod(param1, param2)
     * xxxMethod(param3, param4)
     * <p>
     * 暂不支持 Dubbo 3.x 版本
     *
     * @param endpoint
     * @return
     */
    private String processMessageList(Endpoint endpoint, String message) {
        if (StringUtils.isBlank(message)) {
            return message;
        }

        String[] serviceArray = message.split("(?=(\r\n))");
        String[] igvArray = splitIgv(StringUtils.removeEnd(endpoint.getHostname(), Const.DUBBO_SERVICE_SUFFIX));

        List<StringBuilder> methodListByIgv = new ArrayList<>();
        StringBuilder builder = null;
        for (String stringBySeparator : serviceArray) {
            if (StringUtils.indexOf(stringBySeparator, igvArray[0]) != NumberUtils.INTEGER_MINUS_ONE
                    && DUBBO_TELNET_RETURN_PATTERN.matcher(stringBySeparator).matches()) {
                builder = new StringBuilder();
                builder.append(stringBySeparator);
                methodListByIgv.add(builder);
                continue;
            }
            if (null == builder) {
                continue;
            }
            builder.append(stringBySeparator);
        }

        String dubboTelnetServiceKey = getDubboTelnetServiceKey(igvArray);
        for (StringBuilder stringBuilder : methodListByIgv) {
            String messageInfo = stringBuilder.toString();
            if (messageInfo.indexOf(dubboTelnetServiceKey) != NumberUtils.INTEGER_MINUS_ONE) {
                return messageInfo;
            }
        }
        return message;
    }


    /**
     * 获取Dubbo service 作为Provider 的格式
     *
     * @param igvArray
     * @return
     */
    private String getDubboTelnetServiceKey(String[] igvArray) {
        StringBuilder builder = new StringBuilder();
        //igvArray[1] 指 dubbo group
        if (null != igvArray[1]) {
            builder.append(igvArray[1]).append("/");
        }
        //igvArray[0] 指 dubbo interface
        builder.append(igvArray[0]);
        //igvArray[2] 指 dubbo version
        if (null != igvArray[2]) {
            builder.append(":").append(igvArray[2]);
        }
        builder.append(" (as provider):");
        return builder.toString();
    }


    /**
     * 分离igv{interface:group:version}
     * <p>
     * xxxService ===> new String[]{"xxxService","",""}
     * xxxService:xxxGroup:xxxVersion ===> new String[]{"xxxService","xxxGroup","xxxVersion"}
     * xxxService:xxxGroup ===> new String[]{"xxxService","xxxGroup",""}
     * xxxService::xxxVersion ===> new String[]{"xxxService","","xxxVersion"}
     *
     * @param igv
     * @return
     */
    public static String[] splitIgv(String igv) {
        String[] result = new String[3];
        String[] split = igv.split(":");
        for (int i = 0; i < result.length; i++) {
            result[i] = split.length > i ? split[i] : StringUtils.EMPTY;
        }
        return result;
    }

    @Override
    public PluginOrderDTO getPluginManager(String name){
        HasMetadata config = configManager.getConfig(K8sResourceEnum.PluginManager.name(), name);
        if (Objects.isNull(config)) {
            throw new ApiPlaneException("plugin manager config can not found.");
        }
        PluginOrderDTO pluginOrderDTO = Trans.trans((K8sTypes.PluginManager) config);
        pluginOrderDTO.setName(name);
        return pluginOrderDTO;
    }

    @Override
    public void resortPluginOrder(List<String> names) {
        //获取待更新的plugin manager
        List<HasMetadata> hasMetadatas = names.stream()
                .map(n -> configManager.getConfig(K8sResourceEnum.PluginManager.name(), globalConfig.getResourceNamespace(), n))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(hasMetadatas)){
            return;
        }
        for (HasMetadata hasMetadata : hasMetadatas) {
            //数据转换
            PluginOrderDTO trans = Trans.trans((K8sTypes.PluginManager) hasMetadata);
            //插件排序
            PluginMappingEnum.pluginSort(trans.getPlugins());
            //更新配置
            PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(trans);
            configManager.updateConfig(pluginOrder);
        }
    }

    public List<PluginOrderDTO> getPluginManagerList(String gwCluster){
        List<HasMetadata> config = configManager.getConfigListWithRev(K8sResourceEnum.PluginManager.name());
        if (CollectionUtils.isEmpty(config)){
            return new ArrayList<>();
        }
        return config.stream().map(o -> Trans.trans((K8sTypes.PluginManager) o)).filter(o -> gwCluster.equals(o.getGwCluster())).collect(Collectors.toList());
    }


    private void createEnvoyServicePort(IstioGateway istioGateway){
        List<Service> envoyServiceList = envoyHttpClient.getEnvoyServiceList(istioGateway.getGwCluster());
        List<IstioGatewayServer> servers = istioGateway.getServers();
        if (CollectionUtils.isEmpty(envoyServiceList) || CollectionUtils.isEmpty(servers)){
            return;
        }
        for (Service envoyService : envoyServiceList) {
            boolean needCreate = Boolean.FALSE;
            List<ServicePort> servicePortList = envoyService.getSpec().getPorts();
            List<Integer> ports = servicePortList.stream().map(ServicePort::getPort).collect(Collectors.toList());
            for (IstioGatewayServer server : servers) {
                if (ports.contains(server.getNumber())){
                    //当前端口已存在，不添加
                    continue;
                }
                ServicePort servicePort = new ServicePort();
                String protocol = Const.PROTOCOL_UDP.equalsIgnoreCase(server.getProtocol()) ? Const.PROTOCOL_UDP : Const.PROTOCOL_TCP;
                servicePort.setProtocol(protocol);
                servicePort.setPort(server.getNumber());
                servicePort.setName(server.getProtocol().toLowerCase() + server.getNumber());
                servicePortList.add(servicePort);
                needCreate = Boolean.TRUE;
            }
            if (needCreate){
                configManager.updateK8sService(envoyService);
            }

        }
    }

    private void deleteEnvoyServicePort(IstioGateway istioGateway){
        List<Service> envoyServiceList = envoyHttpClient.getEnvoyServiceList(istioGateway.getGwCluster());
        if (CollectionUtils.isEmpty(envoyServiceList)){
            return;
        }
        List<Integer> deletePort = getDeletePort(istioGateway.getName());
        for (Service service : envoyServiceList) {
            List<ServicePort> servicePorts = service.getSpec().getPorts().stream().filter(o -> !deletePort.contains(o.getPort())).collect(Collectors.toList());
            service.getSpec().setPorts(servicePorts);
            configManager.updateK8sService(service);
        }
    }

    private List<Integer> getDeletePort(String name){
        HasMetadata hasMetadata = configManager.getConfig(K8sResourceEnum.Gateway.name(), name);
        if (hasMetadata == null){
            return Collections.emptyList();
        }
        Gateway gateway = (Gateway)hasMetadata;
        List<Server> servers = gateway.getSpec().getServers();
        if (CollectionUtils.isEmpty(servers)){
            return Collections.emptyList();
        }
        //80端口envoy用于健康检查，默认不删除80端口
        return servers.stream().map(Server::getPort).map(Port::getNumber).distinct().filter(o -> o != 80).collect(Collectors.toList());
    }


    @Override
    public List<EnvoyServiceDTO> getEnvoyAddress(String gwClusterName) {
        Pod envoyPod = envoyHttpClient.getEnvoyPod(gwClusterName);
        if (Boolean.TRUE.equals(envoyPod.getSpec().getHostNetwork())){
            EnvoyServiceDTO serviceDTO = new EnvoyServiceDTO();
            serviceDTO.setGwClusterName(gwClusterName);
            serviceDTO.setServiceType(HOST_NETWORK);
            serviceDTO.setIps(envoyHttpClient.getEnvoySchedulableNodeAddress(gwClusterName));
            return Collections.singletonList(serviceDTO);
        }
        List<EnvoyServiceDTO> envoyServiceDTOS = new ArrayList<>();
        List<Service> services = envoyHttpClient.getEnvoyServiceList(gwClusterName);
        for (Service service : services) {
            EnvoyServiceDTO serviceDTO = new EnvoyServiceDTO();
            serviceDTO.setGwClusterName(gwClusterName);
            serviceDTO.setServiceType(service.getSpec().getType());
            List<EnvoyServicePortDTO> ports = service.getSpec().getPorts().stream().map(Trans::getPorts).collect(Collectors.toList());
            serviceDTO.setPorts(ports);
            serviceDTO.setIps(getIps(serviceDTO.getServiceType(), service.getSpec(), gwClusterName));
            envoyServiceDTOS.add(serviceDTO);
        }
        return envoyServiceDTOS;
    }

    @Override
    public List<KubernetesServiceDTO> getKubernetesService(String namespace, Map<String, String> filters, String domain) {
        List<HasMetadata> configList = configManager.getConfigList(K8sResourceEnum.Service.name());
        return configList.stream().parallel().map(Trans::transService).filter(s -> {
                    if (StringUtils.isNotBlank(namespace) && !s.getNamespace().contains(namespace)){
                        return false;
                    }
                    if (StringUtils.isNotBlank(domain)  && !s.getDomain().contains(domain)) {
                        return false;
                    }
                    return true;
                }
        ).collect(Collectors.toList());
    }

    private List<String> getIps(String type, ServiceSpec spec, String gwClusterName){
        switch (type){
            case NODE_PORT:
                return envoyHttpClient.getEnvoySchedulableNodeAddress(gwClusterName);
            case CLUSTER_IP:
                return Collections.singletonList(spec.getClusterIP());
            case LOAD_BALANCER:
                return spec.getExternalIPs();
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public boolean publishConfigMap(ConfigMapDTO configMapDTO) {
        ConfigMap configMap = k8sClient.getConfigMap(globalConfig.getResourceNamespace(), configMapDTO.getLabel());
        if (configMap == null){
            logger.error("config map not existed, label:{}", configMapDTO.getLabel());
            return false;
        }
        Map<String, String> data = configMap.getData();
        if (data == null){
            data = new HashMap<>();
        }
        //如果value为空，则需要删除key
        if (StringUtils.isBlank(configMapDTO.getContentValue())){
            if (!data.containsKey(configMapDTO.getContentKey())){
               return true;
            }
            data.remove(configMapDTO.getContentKey());
        }else {
            data.put(configMapDTO.getContentKey(), configMapDTO.getContentValue());
        }
        configMap.setData(data);
        configManager.updateConfig(configMap);
        return true;
    }
}
