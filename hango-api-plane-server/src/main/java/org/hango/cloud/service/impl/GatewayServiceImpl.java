package org.hango.cloud.service.impl;

import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.Trans;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.hango.cloud.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import me.snowdrop.istio.slime.v1alpha1.Plugin;
import me.snowdrop.istio.slime.v1alpha1.PluginManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GatewayServiceImpl implements GatewayService {

    private static final String COLON = ":";
    private static final String SERVICE_LOADBALANCER_SIMPLE = "Simple";
    private static final String SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN = "ROUND_ROBIN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN = "LEAST_CONN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_RANDOM = "RANDOM";
    private static final String SERVICE_LOADBALANCER_HASH = "ConsistentHash";
    private static final String SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME = "HttpHeaderName";
    private static final String SERVICE_LOADBALANCER_HASH_HTTPCOOKIE = "HttpCookie";
    private static final String SERVICE_LOADBALANCER_HASH_USESOURCEIP = "UseSourceIp";

    private ResourceManager resourceManager;

    private GatewayConfigManager configManager;

    public GatewayServiceImpl(ResourceManager resourceManager, GatewayConfigManager configManager) {
        this.resourceManager = resourceManager;
        this.configManager = configManager;
    }


    @Override
    public void updateAPI(PortalAPIDTO api) {
        configManager.updateConfig(Trans.portalAPI2API(api));
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
        List<Plugin> plugins = pm.getSpec().getPlugin();
        dto.setPlugins(new ArrayList<>());
        if (CollectionUtils.isEmpty(plugins)) return dto;
        plugins.forEach(p -> {
            PluginOrderItemDTO itemDTO = new PluginOrderItemDTO();
            itemDTO.setEnable(p.getEnable());
            itemDTO.setName(p.getName());
            itemDTO.setSettings(p.getSettings());
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
    public List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId) {
        String pattern = ".*";
        if (!StringUtils.isEmpty(name)) {
            pattern = "^" + name + pattern + "$";
        }
        final Pattern fPattern = Pattern.compile(pattern);
        return resourceManager.getServiceAndPortList().stream()
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
    public List<Gateway> getGatewayList() {
        return resourceManager.getGatewayList();
    }

    @Override
    public void updateGlobalPlugins(GlobalPluginDTO globalPluginDTO) {
        configManager.updateConfig(Trans.globalPluginsDTO2GlobalPlugins(globalPluginDTO));

    }

    @Override
    public void deleteGlobalPlugins(GlobalPluginsDeleteDTO globalPluginsDeleteDTO) {
        configManager.deleteConfig(Trans.globalPluginsDeleteDTO2GlobalPlugins(globalPluginsDeleteDTO));
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
//        istioGateway.setXffNumTrustedHops(server.getXffNumTrustedHops());
//        istioGateway.setCustomIpAddressHeader(server.getCustomIpAddressHeader());
//        istioGateway.setUseRemoteAddress(server.getUseRemoteAddress() == null ? null : String.valueOf(server.getUseRemoteAddress()));
        return Trans.GW2portal(istioGateway);
    }
}
