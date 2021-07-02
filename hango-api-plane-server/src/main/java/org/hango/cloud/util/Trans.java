package org.hango.cloud.util;

import com.google.common.collect.ImmutableList;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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
        api.setServiceTag(portalAPI.getServiceTag());
        api.setApiId(portalAPI.getRouteId());
        api.setApiName(portalAPI.getRouteName());
        api.setTenantId(portalAPI.getTenantId());
        api.setProjectId(portalAPI.getProjectId());
        //timeout 默认60000ms
        if (api.getTimeout() == null) api.setTimeout(60000L);
        if (portalAPI.getHttpRetry() != null && portalAPI.getHttpRetry() instanceof HttpRetryDTO
        && portalAPI.getHttpRetry().isRetry()){
            api.setAttempts(portalAPI.getHttpRetry().getAttempts());
            api.setPerTryTimeout(portalAPI.getHttpRetry().getPerTryTimeout());
            api.setRetryOn(portalAPI.getHttpRetry().getRetryOn());
        }
        api.setRequestOperation(requestOperationDTO2requestOperation(portalAPI.getRequestOperation()));
        if (portalAPI.getVirtualClusterDTO() != null){
            api.setVirtualClusterName(portalAPI.getVirtualClusterDTO().getVirtualClusterName());
            api.setVirtualClusterHeaders(pairsDTO2Pairs(portalAPI.getVirtualClusterDTO().getHeaders()));
        }
        return api;
    }

    private static RequestOperation requestOperationDTO2requestOperation(RequestOperationDTO requestOperation) {
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
        return istioGateway;
    }

    public static PortalIstioGatewayDTO GW2portal(IstioGateway istioGateway) {
        if (istioGateway == null) {
            return null;
        }
        PortalIstioGatewayDTO portalIstioGatewayDTO = new PortalIstioGatewayDTO();
        portalIstioGatewayDTO.setName(istioGateway.getName());
        portalIstioGatewayDTO.setGwCluster(istioGateway.getGwCluster());
        return portalIstioGatewayDTO;
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

        PluginOrder po = new PluginOrder();
        po.setGatewayLabels(pluginOrderDTO.getGatewayLabels());
        List<String> orderItems = new ArrayList<>();
        for (PluginOrderItemDTO dto : pluginOrderDTO.getPlugins()) {
            if (Objects.nonNull(dto)) {
                orderItems.add(ResourceGenerator.newInstance(dto, ResourceType.OBJECT).yamlString());
            }
        }
        po.setPlugins(orderItems);
        return po;
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
        return api;
    }

    public static GlobalPlugin globalPluginsDTO2GlobalPlugins(GlobalPluginDTO globalPluginsDTO) {

        GlobalPlugin gp = new GlobalPlugin();
        gp.setCode(globalPluginsDTO.getCode());
        gp.setGateway(globalPluginsDTO.getGateway());
        gp.setHosts(globalPluginsDTO.getHosts());
        gp.setPlugins(globalPluginsDTO.getPlugins());
        return gp;
    }

    public static GlobalPlugin globalPluginsDeleteDTO2GlobalPlugins(GlobalPluginsDeleteDTO globalPluginsDeleteDTO) {

        GlobalPlugin gp = new GlobalPlugin();
        gp.setCode(globalPluginsDeleteDTO.getCode());
        gp.setPlugins(globalPluginsDeleteDTO.getPlugins());
        return gp;
    }

    public static ValidateResultDTO validateResult2ValidateResultDTO(ValidateResult validateResult) {
        ValidateResultDTO dto = new ValidateResultDTO();
        dto.setPass(validateResult.isPass());
        if (validateResult.getItems() != null) {
            dto.setItems(validateResult.getItems());
        }
        return dto;
    }
}
