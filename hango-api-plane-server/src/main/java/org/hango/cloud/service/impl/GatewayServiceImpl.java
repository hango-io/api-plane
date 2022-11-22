package org.hango.cloud.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.istio.PilotHttpClient;
import org.hango.cloud.core.template.TemplateTranslator;
import org.hango.cloud.k8s.K8sTypes.PluginManager;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.IstioGateway;
import org.hango.cloud.meta.PluginOrder;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.service.GatewayService;
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
import org.springframework.util.CollectionUtils;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GatewayServiceImpl implements GatewayService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceImpl.class);

    private static final String COLON = ":";
    private static final String SERVICE_LOADBALANCER_SIMPLE = "Simple";
    private static final String SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN = "ROUND_ROBIN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN = "LEAST_CONN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_RANDOM = "RANDOM";
    private static final String SERVICE_LOADBALANCER_HASH = "ConsistentHash";
    private static final String SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME = "HttpHeaderName";
    private static final String SERVICE_LOADBALANCER_HASH_HTTPCOOKIE = "HttpCookie";
    private static final String SERVICE_LOADBALANCER_HASH_USESOURCEIP = "UseSourceIp";
    private static final String DUBBO_TELNET_COMMAND_TEMPLATE = "ls -l %s";
    private static final String DUBBO_TELNET_COMMAND_END_PATTERN = "dubbo>";
    private static final Pattern DUBBO_INFO_PATTRERN = Pattern.compile("^(\\S*) (\\S*)\\((\\S*)\\)$");
    private static final Pattern DUBBO_TELNET_RETURN_PATTERN = Pattern.compile("[\\s\\S]*?\\(as (provider|consumer)\\):");


    private ResourceManager resourceManager;

    private GatewayConfigManager configManager;

    private GlobalConfig globalConfig;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private TemplateTranslator templateTranslator;

    @Autowired
    private PilotHttpClient pilotHttpClient;

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
     * 调用发布插件接口，做DTO->POJO转换
     *
     * @param plugin 路由插件DTO对象
     */
    @Override
    public void updateGatewayPlugin(GatewayPluginDTO plugin) {
        configManager.updateConfig(Trans.pluginDTOToPlugin(plugin));
    }

    /**
     * 调用更新插件接口，做DTO->POJO转换（对于插件CRD而言，删除的本质还是在更新CRD配置）
     *
     * @param plugin 插件DTO对象
     */
    @Override
    public void deleteGatewayPlugin(GatewayPluginDTO plugin) {
        configManager.updateConfig(Trans.pluginDTOToPlugin(plugin));
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
    public PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto) {
        pluginOrderDto.setPlugins(new ArrayList<>());
        PluginOrderDTO dto = new PluginOrderDTO();
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDto);
        HasMetadata config = configManager.getConfig(pluginOrder);
        if (Objects.isNull(config)) throw new ApiPlaneException("plugin manager config can not found.");
        PluginManager pm = (PluginManager) config;
        dto.setGatewayLabels(pm.getSpec().getWorkloadLabels());
        List<PluginManagerOuterClass.Plugin> plugins = pm.getSpec().getPluginList();
        dto.setPlugins(new ArrayList<>());
        if (CollectionUtils.isEmpty(plugins)) return dto;
        plugins.forEach(p -> {
            PluginOrderItemDTO itemDTO = new PluginOrderItemDTO();
            itemDTO.setEnable(p.getEnable());
            itemDTO.setName(p.getName());
            itemDTO.setInline(p.getInline());
            itemDTO.setListenerType(p.getListenerTypeValue());
            itemDTO.setPort(p.getPort());
            dto.getPlugins().add(itemDTO);
        });
        return dto;
    }

    @Override
    public void updatePluginOrder(PluginOrderDTO pluginOrderDto) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDto);
        configManager.updateConfig(pluginOrder);
    }

    @Override
    public void deletePluginOrder(PluginOrderDTO pluginOrderDTO) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDTO);
        configManager.deleteConfig(pluginOrder);
    }

    @Override
    public List<String> getServiceList() {
        return resourceManager.getServiceList();
    }

    @Override
    public List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId, Map<String, String> filters) {
        logger.info("[get service] start getServiceAndPortList, name: {}, type: {}, registryId: {}", name, type, registryId);
        String pattern = ".*";
        if (!StringUtils.isEmpty(name)) {
            pattern = "^" + name + pattern + "$";
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
    public void updateIstioGateway(PortalIstioGatewayDTO portalGateway) {
        configManager.updateConfig(Trans.portalGW2GW(portalGateway));
    }


    @Override
    public PortalIstioGatewayDTO getIstioGateway(String clusterName) {
        IstioGateway istioGateway = new IstioGateway();
        istioGateway.setGwCluster(clusterName);
        IstioResource config = (IstioResource) configManager.getConfig(istioGateway);
        if (config == null) {
            return null;
        }
        GatewaySpec spec = (GatewaySpec) config.getSpec();
        final String gwCluster = "gw_cluster";
        Map<String, String> selector = spec.getSelector();
        if (CollectionUtils.isEmpty(selector)) {
            selector.get(gwCluster);
        }
        istioGateway.setName(config.getMetadata().getName());
        if (CollectionUtils.isEmpty(spec.getServers()) || spec.getServers().get(0) == null) {
            return null;
        }
        Server server = spec.getServers().get(0);
        istioGateway.setXffNumTrustedHops(server.getXffNumTrustedHops());
        istioGateway.setCustomIpAddressHeader(server.getCustomIpAddressHeader());
        istioGateway.setUseRemoteAddress(server.getUseRemoteAddress() == null ? null : String.valueOf(server.getUseRemoteAddress()));
        return Trans.GW2portal(istioGateway);
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
}
