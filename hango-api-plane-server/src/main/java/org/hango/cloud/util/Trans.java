package org.hango.cloud.util;

import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.meta.enums.CRDMetaEnum;
import org.hango.cloud.meta.enums.UriMatch;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.service.impl.GatewayServiceImpl.GW_CLUSTER;


public class Trans {
    public static API portalAPI2API(PortalAPIDTO portalAPI) {

        API api = new API();
        BeanUtils.copyProperties(portalAPI, api);
        api.setUriMatch(UriMatch.get(portalAPI.getUriMatch()));
        api.setProxyServices(portalAPI.getProxyServices().stream()
                .map(ps -> portalRouteService2Service(ps))
                .collect(Collectors.toList()));
        api.setGateways(Arrays.asList(portalAPI.getGateway().toLowerCase()));
        api.setName(portalAPI.getCode());

        api.setHeaders(pairsDTO2Pairs(portalAPI.getHeaders()));
        api.setQueryParams(pairsDTO2Pairs(portalAPI.getQueryParams()));
        api.setPriority(portalAPI.getPriority());
        api.setApiName(portalAPI.getRouteName());
        api.setTenantId(portalAPI.getTenantId());
        api.setProjectId(portalAPI.getProjectId());
        Long version = portalAPI.getVersion();
        api.setVersion(version == null ? 0 : version);
        //timeout 默认60000ms
        //0512 to cm, remove default timeout
//        if (api.getTimeout() == null) api.setTimeout(60000L);
        if (portalAPI.getHttpRetry() != null && portalAPI.getHttpRetry() instanceof HttpRetryDTO
        && portalAPI.getHttpRetry().isRetry()){
            api.setAttempts(portalAPI.getHttpRetry().getAttempts());
            api.setPerTryTimeout(portalAPI.getHttpRetry().getPerTryTimeout());
            api.setRetryOn(portalAPI.getHttpRetry().getRetryOn());
        }
        api.setRequestOperation(requestOperationDTO2requestOperation(portalAPI.getRequestOperation()));
        //流量镜像配置
        if(portalAPI.getMirrorTrafficDto() != null){
            PortalMirrorTrafficDto mirrorTrafficDto = portalAPI.getMirrorTrafficDto();
            Service mirrorTraffic = new Service();
            mirrorTraffic.setBackendService(mirrorTrafficDto.getBackendService());
            mirrorTraffic.setPort(mirrorTrafficDto.getPort());
            mirrorTraffic.setSubset(mirrorTrafficDto.getSubset());
            api.setMirrorTraffic(mirrorTraffic);
        }
        api.setMetaMap(portalAPI.getMetaMap());
        api.setProtocol(portalAPI.getProtocol());
        return api;
    }

    public static EnvoyServicePortDTO getPorts(ServicePort port){
        EnvoyServicePortDTO envoyServicePortDTO = new EnvoyServicePortDTO();
        envoyServicePortDTO.setName(port.getName());
        envoyServicePortDTO.setProtocol(port.getProtocol());
        envoyServicePortDTO.setPort(port.getPort());
        envoyServicePortDTO.setNodePort(port.getNodePort());
        return envoyServicePortDTO;
    }


    private static RequestOperation requestOperationDTO2requestOperation(
        RequestOperationDTO requestOperation) {
        if (requestOperation == null) return null;
        RequestOperation ro = new RequestOperation();
        ro.setAdd(new HashMap<>(requestOperation.getAdd()));
        return ro;
    }

    public static IstioGateway portalGW2GW(PortalIstioGatewayDTO portalGateway) {
        if (portalGateway == null) {
            return null;
        }
        IstioGateway istioGateway = new IstioGateway();
        istioGateway.setName(portalGateway.getName());
        istioGateway.setGwCluster(portalGateway.getGwCluster());
        if (CollectionUtils.isEmpty(portalGateway.getServers())){
            return istioGateway;
        }
        List<IstioGatewayServer> istioGatewayServers = new ArrayList<>();
        for (PortalIstioGatewayServerDTO server : portalGateway.getServers()) {
            IstioGatewayServer istioGatewayServer = new IstioGatewayServer();
            String name = server.getProtocol().toLowerCase();

            istioGatewayServer.setProtocol(server.getProtocol());
            istioGatewayServer.setNumber(server.getNumber());
            istioGatewayServer.setHosts(server.getHosts());
            PortalIstioGatewayTLSDTO portalIstioGatewayTLSDTO = server.getPortalIstioGatewayTLSDTO();
            if (Const.PROTOCOL_HTTPS.equals(server.getProtocol()) && portalIstioGatewayTLSDTO != null){
                IstioGatewayTLS istioGatewayTLS = new IstioGatewayTLS();
                istioGatewayTLS.setMode(portalIstioGatewayTLSDTO.getMode());
                istioGatewayTLS.setCredentialName(portalIstioGatewayTLSDTO.getCredentialName());
                if (!StringUtils.isEmpty(portalIstioGatewayTLSDTO.getCredentialName())){
                    name = portalIstioGatewayTLSDTO.getCredentialName();
                }
                istioGatewayServer.setIstioGatewayTLS(istioGatewayTLS);
            }
            istioGatewayServer.setName(name);
            istioGatewayServers.add(istioGatewayServer);
        }
        istioGateway.setServers(istioGatewayServers);
        return istioGateway;
    }

    public static Service portalRouteService2Service(PortalRouteServiceDTO portalRouteService) {
        Service s = new Service();
        s.setCode(portalRouteService.getCode().toLowerCase());
        s.setType(portalRouteService.getType());
        s.setWeight(portalRouteService.getWeight());
        s.setBackendService(portalRouteService.getBackendService());

        Integer port = portalRouteService.getPort();
        if (portalRouteService.getType().equals(Const.PROXY_SERVICE_TYPE_DYNAMIC) && port == null) {
            throw new ApiPlaneException("dynamic service must have port " + s.getCode());
        }
        s.setPort(port);
        s.setSubset(portalRouteService.getSubset());
        return s;
    }

    public static Service portalService2Service(PortalServiceDTO portalService) {

        Service s = new Service();
        s.setCode(portalService.getCode().toLowerCase());
        s.setType(portalService.getType());
        s.setWeight(portalService.getWeight());
        Long version = portalService.getVersion();
        s.setVersion(version == null ? 0 : version);
        s.setBackendService(portalService.getBackendService());
        if (!StringUtils.isEmpty(portalService.getGateway())) {
            s.setGateway(portalService.getGateway().toLowerCase());
        }
        s.setProtocol(portalService.getProtocol());
        if (portalService.getTrafficPolicy() != null) {
            PortalTrafficPolicyDTO trafficPolicy = portalService.getTrafficPolicy();
            PortalOutlierDetectionDTO outlierDetection = trafficPolicy.getOutlierDetection();
            PortalHealthCheckDTO healthCheck = trafficPolicy.getHealthCheck();
            PortalLoadBalancerDTO loadBalancer = trafficPolicy.getLoadBalancer();
            PortalServiceConnectionPoolDTO serviceConnectionPool = trafficPolicy.getConnectionPool();

            if (outlierDetection != null) {
                s.setConsecutiveErrors(outlierDetection.getConsecutiveErrors());
                s.setBaseEjectionTime(outlierDetection.getBaseEjectionTime());
                s.setMaxEjectionPercent(outlierDetection.getMaxEjectionPercent());
                s.setMinHealthPercent(outlierDetection.getMinHealthPercent());
            }

            if (healthCheck != null) {
                s.setPath(healthCheck.getPath());
                s.setTimeout(healthCheck.getTimeout());
                s.setExpectedStatuses(healthCheck.getExpectedStatuses());
                s.setHealthyInterval(healthCheck.getHealthyInterval());
                s.setHealthyThreshold(healthCheck.getHealthyThreshold());
                s.setUnhealthyInterval(healthCheck.getUnhealthyInterval());
                s.setUnhealthyThreshold(healthCheck.getUnhealthyThreshold());
            }

            if (loadBalancer != null) {
                s.setLoadBalancer(serviceLBDTO2ServiceLB(loadBalancer));
            }

            if (serviceConnectionPool != null) {
                s.setConnectionPool(serviceConnectionPool);
            }
        }

        s.setServiceTag(portalService.getServiceTag());
        s.setSubsets(subsetDTO2Subset(portalService.getSubsets()));
        s.setMetaMap(portalService.getMetaMap());
        return s;
    }

    private static Service.ServiceLoadBalancer serviceLBDTO2ServiceLB(PortalLoadBalancerDTO loadBalancerDTO) {

        if (loadBalancerDTO == null) return null;
        Service.ServiceLoadBalancer serviceLoadBalancer = new Service.ServiceLoadBalancer();

        if (!StringUtils.isEmpty(loadBalancerDTO.getSimple())) {
            // round robin, random, least conn
            serviceLoadBalancer.setSimple(loadBalancerDTO.getSimple());
        } else if (loadBalancerDTO.getConsistentHashDTO() != null){
            // consistent hash
            PortalLoadBalancerDTO.ConsistentHashDTO consistentHashDTO = loadBalancerDTO.getConsistentHashDTO();
            Service.ServiceLoadBalancer.ConsistentHash consistentHash = new Service.ServiceLoadBalancer.ConsistentHash();
            if (consistentHashDTO.getUseSourceIp() != null) {
                consistentHash.setUseSourceIp(consistentHashDTO.getUseSourceIp());
            } else if (!StringUtils.isEmpty(consistentHashDTO.getHttpHeaderName())) {
                consistentHash.setHttpHeaderName(consistentHashDTO.getHttpHeaderName());
            } else if (consistentHashDTO.getHttpCookie() != null) {
                PortalLoadBalancerDTO.ConsistentHashDTO.HttpCookieDTO httpCookieDTO = consistentHashDTO.getHttpCookie();
                Service.ServiceLoadBalancer.ConsistentHash.HttpCookie httpCookie = new Service.ServiceLoadBalancer.ConsistentHash.HttpCookie();
                httpCookie.setName(httpCookieDTO.getName());
                httpCookie.setPath(httpCookieDTO.getPath());
                httpCookie.setTtl(httpCookieDTO.getTtl());
                consistentHash.setHttpCookie(httpCookie);
            }
            serviceLoadBalancer.setConsistentHash(consistentHash);
        }
        serviceLoadBalancer.setSlowStartWindow(loadBalancerDTO.getSlowStartWindow());
        serviceLoadBalancer.setLocalitySetting(loadBalancerDTO.getLocalitySetting());
        return serviceLoadBalancer;
    }

    private static List<ServiceSubset> subsetDTO2Subset(List<ServiceSubsetDTO> subsets) {
        if (CollectionUtils.isEmpty(subsets)) return Collections.emptyList();
        return subsets.stream()
                    .map(sd -> {
                        ServiceSubset ss = new ServiceSubset();
                        ss.setLabels(sd.getLabels());
                        ss.setName(sd.getName());
                        ss.setTrafficPolicy(subsetTrafficPolicyDtoTosubsetTrafficPolicy(sd.getTrafficPolicy()));
                        ss.setStaticAddrs(sd.getStaticAddrs());
                        if(!CollectionUtils.isEmpty(sd.getMetaMap())){
                            ss.setMetaLabelMap(sd.getMetaMap().get(CRDMetaEnum.DESTINATION_RULE_STATS_META.getName()));
                        }
                        return ss;
                    })
                    .collect(Collectors.toList());
    }

    /**
     * 主要是将subset中的ServiceLoadBalancer生成出来
     *
     * @param portalTrafficPolicyDTO
     * @return
     */
    private static ServiceSubset.TrafficPolicy subsetTrafficPolicyDtoTosubsetTrafficPolicy
            (PortalTrafficPolicyDTO portalTrafficPolicyDTO) {
        if (portalTrafficPolicyDTO == null) {
            return null;
        }

        ServiceSubset.TrafficPolicy trafficPolicy = new ServiceSubset.TrafficPolicy();
        trafficPolicy.setHealthCheck(portalTrafficPolicyDTO.getHealthCheck());
        trafficPolicy.setOutlierDetection(portalTrafficPolicyDTO.getOutlierDetection());
        trafficPolicy.setLoadBalancer(serviceLBDTO2ServiceLB(portalTrafficPolicyDTO.getLoadBalancer()));
        trafficPolicy.setConnectionPool(portalTrafficPolicyDTO.getConnectionPool());
        return trafficPolicy;
    }

    public static PluginOrder pluginOrderDTO2PluginOrder(PluginOrderDTO pluginOrderDTO) {
        if (pluginOrderDTO == null) {
            return null;
        }
        PluginOrder po = new PluginOrder();
        Map<String, String> gatewayLabels = new HashMap<>();
        gatewayLabels.put(GW_CLUSTER, pluginOrderDTO.getGwCluster());
        po.setGatewayLabels(gatewayLabels);
        po.setName(pluginOrderDTO.getName());
        List<String> orderItems = new ArrayList<>();
        List<PluginOrderItemDTO> plugins = pluginOrderDTO.getPlugins();
        if (CollectionUtils.isEmpty(plugins)){
            return po;
        }
        for (PluginOrderItemDTO dto : plugins) {
            if (Objects.nonNull(dto)) {
                orderItems.add(PluginGenerator.newInstance(dto, ResourceType.OBJECT).yamlString());
            }
        }
        po.setPlugins(orderItems);
        return po;
    }

    public static PluginOrderDTO trans(K8sTypes.PluginManager pluginManager){
        PluginOrderDTO dto = new PluginOrderDTO();
        Map<String, String> workloadLabels = pluginManager.getSpec().getWorkloadLabels();
        dto.setGwCluster(workloadLabels.get(GW_CLUSTER));
        dto.setPlugins(new ArrayList<>());
        dto.setName(pluginManager.getMetadata().getName());
        List<PluginManagerOuterClass.Plugin> plugins = pluginManager.getSpec().getPluginList();
        if (CollectionUtils.isEmpty(plugins)) {
            return dto;
        }
        plugins.forEach(p -> {
            PluginOrderItemDTO itemDTO = new PluginOrderItemDTO();
            itemDTO.setEnable(p.getEnable());
            itemDTO.setName(p.getName());
            buildPluginSetting(itemDTO, p);
            itemDTO.setPort(p.getPort());
            dto.getPlugins().add(itemDTO);
        });
        return dto;
    }

    public static void buildPluginSetting(PluginOrderItemDTO itemDTO, PluginManagerOuterClass.Plugin plugin){
        if (StringUtils.hasText(plugin.getRider().getPluginName())){
            itemDTO.setRider(trans(plugin.getRider()));
        }else if (StringUtils.hasText(plugin.getWasm().getPluginName())){
            itemDTO.setWasm(trans(plugin.getWasm()));
        }else {
            itemDTO.setInline(plugin.getInline());
        }

    }

    private static RiderDTO trans(slime.microservice.plugin.v1alpha1.PluginManagerOuterClass.Rider rider){
        RiderDTO dto = new RiderDTO();
        dto.setPluginName(rider.getPluginName());
        dto.setUrl(rider.getUrl());
        dto.setImagePullSecretName(rider.getImagePullSecretName());
        dto.setSettings(rider.getSettings());
        return dto;
    }

    private static RiderDTO trans(PluginManagerOuterClass.Wasm wasm){
        RiderDTO dto = new RiderDTO();
        dto.setPluginName(wasm.getPluginName());
        dto.setUrl(wasm.getUrl());
        dto.setImagePullSecretName(wasm.getImagePullSecretName());
        dto.setSettings(wasm.getSettings());
        return dto;
    }

    public static Secret secretDTO2Secret(PortalSecretDTO portalSecretDTO) {
        Secret secret = new Secret();
        secret.setName(portalSecretDTO.getName());
        secret.setCaCrt(portalSecretDTO.getCaCrt());
        secret.setServerCrt(portalSecretDTO.getServerCrt());
        secret.setServerKey(portalSecretDTO.getServerKey());
        return secret;
    }

    public static EnvoyFilterOrder transEnvoyFilter(EnvoyFilterDTO envoyFilterDTO) {
        EnvoyFilterOrder efo = new EnvoyFilterOrder();
        efo.setPortNumber(envoyFilterDTO.getPortNumber());
        efo.setName(envoyFilterDTO.getName());
        efo.setGwCluster(envoyFilterDTO.getGwCluster());
        return efo;
    }

    private static List<PairMatch> pairsDTO2Pairs(List<PairMatchDTO> pairMatchDTOS) {
        if (CollectionUtils.isEmpty(pairMatchDTOS)) return Collections.emptyList();
        return pairMatchDTOS.stream()
                .map(dto -> pairDTO2Pair(dto))
                .collect(Collectors.toList());
    }

    private static PairMatch pairDTO2Pair(PairMatchDTO pairMatchDTO) {
        PairMatch pm = new PairMatch();
        if (pairMatchDTO == null) return pm;
        pm.setType(pairMatchDTO.getType());
        pm.setKey(pairMatchDTO.getKey());
        pm.setValue(pairMatchDTO.getValue());
        return pm;
    }

    public static API portalDeleteAPI2API(PortalAPIDeleteDTO portalAPI) {

        API api = new API();
        api.setGateways(Arrays.asList(portalAPI.getGateway().toLowerCase()));
        api.setName(portalAPI.getCode());
        api.setMethods(Collections.EMPTY_LIST);
        api.setUriMatch(UriMatch.regex);
        api.setRequestUris(Collections.EMPTY_LIST);
        api.setHosts(Collections.EMPTY_LIST);
        api.setProxyServices(ImmutableList.of(new Service()));
        api.setPlugins(portalAPI.getPlugins());
        api.setProjectId(portalAPI.getProjectId());
        return api;
    }


    /**
     * 插件DTO对象转业务流转对象
     *
     * @param dto 来自其他服务的DTO对象
     * @return 业务流转的网关插件对象
     */
    public static GatewayPlugin pluginDTOToPlugin(GatewayPluginDTO dto) {
        GatewayPlugin gatewayPlugin = new GatewayPlugin();
        gatewayPlugin.setPlugins(dto.getPlugins());
        gatewayPlugin.setGateway(dto.getGateway());
        gatewayPlugin.setHosts(dto.getHosts());
        gatewayPlugin.setCode(dto.getCode());
        gatewayPlugin.setGwCluster(dto.getGwCluster());
        gatewayPlugin.setPluginScope(dto.getPluginScope());
        gatewayPlugin.setPort(dto.getPort() == null ? 80 : dto.getPort());
        Long version = dto.getVersion();
        gatewayPlugin.setVersion(version == null ? 0 : version);
        return gatewayPlugin;
    }

    public static BasePlugin trans(BasePluginDTO dto) {
        BasePlugin basePlugin = new BasePlugin();
        basePlugin.setPluginType(dto.getPluginType());
        basePlugin.setLanguage(dto.getLanguage());
        basePlugin.setName(dto.getName());
        basePlugin.setPluginConfig(dto.getPluginConfig());
        return basePlugin;
    }

    public static KubernetesServiceDTO transService(HasMetadata data) {
        if (data == null) {
            return null;
        }
        io.fabric8.kubernetes.api.model.Service service = (io.fabric8.kubernetes.api.model.Service) data;
        KubernetesServiceDTO kubernetesServiceDTO = new KubernetesServiceDTO();
        ObjectMeta metadata = service.getMetadata();
        if (Objects.isNull(metadata)){
            return null;
        }
        kubernetesServiceDTO.setNamespace(metadata.getNamespace());
        kubernetesServiceDTO.setName(metadata.getName());
        ServiceSpec spec = service.getSpec();
        if (Objects.isNull(spec)){
            return null;
        }
        kubernetesServiceDTO.setType(spec.getType());
        kubernetesServiceDTO.setClusterIP(spec.getClusterIP());
        kubernetesServiceDTO.setPorts(spec.getPorts().stream().map(Trans::transPort).collect(Collectors.toList()));
        return kubernetesServiceDTO;
    }

    public static KubernetesServiceDTO.ServicePort transPort(ServicePort servicePort) {
        if (servicePort == null) {
            return null;
        }
        KubernetesServiceDTO.ServicePort port = new KubernetesServiceDTO.ServicePort();
        port.setName(servicePort.getName());
        port.setPort(servicePort.getPort());
        port.setProtocol(servicePort.getProtocol());
        port.setTargetPort(servicePort.getTargetPort().getIntVal());
        port.setNodePort(servicePort.getNodePort());
        return port;
    }

    public static PluginOrderItemDTO trans(CustomPluginPublishDTO customPluginPublishDTO){
        PluginOrderItemDTO pluginOrderItemDTO = new PluginOrderItemDTO();
        pluginOrderItemDTO.setEnable(false);
        pluginOrderItemDTO.setName(customPluginPublishDTO.getPluginName());
        pluginOrderItemDTO.setWasm(customPluginPublishDTO.getWasm());
        pluginOrderItemDTO.setRider(customPluginPublishDTO.getLua());
        pluginOrderItemDTO.setPort(customPluginPublishDTO.getPort());
        return pluginOrderItemDTO;
    }

}
