package org.hango.cloud.util;


import com.google.common.collect.ImmutableList;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.PairMatch;
import org.hango.cloud.meta.Service;
import org.hango.cloud.meta.dto.PairMatchDTO;
import org.hango.cloud.meta.dto.PortalAPIDTO;
import org.hango.cloud.meta.dto.PortalRouteServiceDTO;
import org.hango.cloud.meta.dto.PortalServiceDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TransTest {

    @Test
    public void testPortalAPI2API() {

        String gateway = "gw1";
        List<String> hosts = ImmutableList.of("h1", "h2");
        List<String> methods = ImmutableList.of("GET", "POST");
        String code = "api";
        List<String> plugins = ImmutableList.of("p1", "p2");
        List<PortalRouteServiceDTO> services = ImmutableList.of(getPortalRouteServiceDTO("/a", "code1", "dynamic", 100));
        List<String> requestUris = ImmutableList.of("/a", "/b");
        String uriMatch = "prefix";
        List<PairMatchDTO> headers = ImmutableList.of(getPairMatch("k1", "v1", "exact"), getPairMatch("k2", "v2", "regex"));
        List<PairMatchDTO> queryParams = ImmutableList.of(getPairMatch("k3", "v3", "exact"), getPairMatch("k4", "v4", "regex"));

        PortalAPIDTO dto = getPortalAPIDTO(gateway, hosts, methods, code, plugins, services, requestUris,
                uriMatch, headers, queryParams);

        API api = Trans.portalAPI2API(dto);

        Assert.assertEquals(api.getGateways(), Arrays.asList(dto.getGateway()));
        Assert.assertEquals(api.getRequestUris(), dto.getRequestUris());
        Assert.assertEquals(api.getHosts(), dto.getHosts());
        Assert.assertEquals(api.getMethods(), dto.getMethods());
        Assert.assertEquals(api.getName(), dto.getCode());
        Assert.assertEquals(api.getPlugins(), dto.getPlugins());
        Assert.assertTrue(equalService(api.getProxyServices(), dto.getProxyServices()));
        Assert.assertEquals(api.getUriMatch().name().toLowerCase(), dto.getUriMatch());
        Assert.assertTrue(equalsPairMatch(api.getHeaders(), dto.getHeaders()));
        Assert.assertTrue(equalsPairMatch(api.getQueryParams(), dto.getQueryParams()));

    }


    @Test
    public void portalService2Service() {

        String service = "service";
        String code = "code";
        String gateway = "gw1";
        String type = "dynamic";
        int weight = 0;

        PortalServiceDTO dto = getPortalServiceDTO(service, code, gateway, type, weight);
        Service portalService = Trans.portalService2Service(dto);

        Assert.assertEquals(portalService.getCode(), dto.getCode());
        Assert.assertEquals(portalService.getBackendService(), dto.getBackendService());
        Assert.assertEquals(portalService.getType(), dto.getType());
        Assert.assertEquals(portalService.getGateway(), dto.getGateway());

    }

    private boolean equalService(List<Service> services, List<PortalRouteServiceDTO> serviceDTOS) {

        if (services.size() != serviceDTOS.size()) return false;

        for (int i = 0; i < services.size(); i++) {
            Service service = services.get(i);
            PortalRouteServiceDTO serviceDTO = serviceDTOS.get(i);
            if (!service.getType().equals(serviceDTO.getType()) ||
                    !service.getBackendService().equals(serviceDTO.getBackendService()) ||
                    !service.getCode().equals(serviceDTO.getCode()) ||
                    !service.getWeight().equals(serviceDTO.getWeight())) {
                return false;
            }
        }

        return true;
    }

    private boolean equalsPairMatch(List<PairMatch> pms, List<PairMatchDTO> pmDTOs) {

        if (pms.size() != pmDTOs.size()) return false;

        for (int i = 0; i < pms.size(); i++) {
            PairMatch pm = pms.get(i);
            PairMatchDTO pmDTO = pmDTOs.get(i);
            if (!pm.getKey().equals(pmDTO.getKey()) ||
                !pm.getValue().equals(pmDTO.getValue()) ||
                !pm.getType().equals(pmDTO.getType())) {
                return false;
            }
        }
        return true;
    }

    private PortalAPIDTO getPortalAPIDTO(String gateway, List<String> hosts, List<String> methods, String code,
                                         List<String> plugins, List<PortalRouteServiceDTO> services, List<String> requestUris,
                                         String uriMatch, List<PairMatchDTO> headers, List<PairMatchDTO> queryParams) {

        PortalAPIDTO dto = new PortalAPIDTO();
        dto.setGateway(gateway);
        dto.setHosts(hosts);
        dto.setMethods(methods);
        dto.setCode(code);
        dto.setPlugins(plugins);
        dto.setProxyServices(services);
        dto.setRequestUris(requestUris);
        dto.setUriMatch(uriMatch);
        dto.setHeaders(headers);
        dto.setQueryParams(queryParams);
        return dto;
    }


    private PortalServiceDTO getPortalServiceDTO(String service, String code, String gateway, String type, int weight) {
        PortalServiceDTO dto = new PortalServiceDTO();
        dto.setBackendService(service);
        dto.setCode(code);
        dto.setGateway(gateway);
        dto.setType(type);
        dto.setWeight(weight);
        return dto;
    }

    private PortalRouteServiceDTO getPortalRouteServiceDTO(String service, String code, String type, int weight) {
        PortalRouteServiceDTO dto = new PortalRouteServiceDTO();
        dto.setBackendService(service);
        dto.setCode(code);
        dto.setType(type);
        dto.setWeight(weight);
        return dto;
    }

    private PairMatchDTO getPairMatch(String k, String v, String type) {
        PairMatchDTO pm = new PairMatchDTO();
        pm.setKey(k);
        pm.setValue(v);
        pm.setType(type);
        return pm;
    }
}
