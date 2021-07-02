package org.hango.cloud.core.gateway.service.impl;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.mock.MockEventPublisher;
import org.hango.cloud.mock.MockK8sConfigStore;
import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.GatewayIstioModelEngine;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.meta.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;


public class GatewayConfigManagerImplTest extends BaseTest {

    @Autowired
    GatewayIstioModelEngine modelEngine;

    @MockBean
    ResourceManager resourceManager;

    @Autowired
    GlobalConfig globalConfig;

    GatewayConfigManagerImpl mockConfigManager;
    MockK8sConfigStore mockK8sConfigStore;

    List<Endpoint> fixedEndpoints = Arrays.asList(buildEndpoint("a.default", "www.testa.com", 80),
                                                  buildEndpoint("b.default", "www.testb.com", 80));

    List<Gateway> fixedGateways = Arrays.asList(buildGateway("demo.gateway-system.svc.cluster.local", "demo",
                                                             ImmutableMap.of("gw_cluster", "demo")));


    @Before
    public void before() {
        when(resourceManager.getEndpointList()).thenReturn(fixedEndpoints);
        when(resourceManager.getGatewayList()).thenReturn(fixedGateways);

        mockK8sConfigStore = new MockK8sConfigStore();
        mockConfigManager = new GatewayConfigManagerImpl(modelEngine, mockK8sConfigStore, globalConfig, new MockEventPublisher());
    }

    @Test
    public void testUpdateAPI() {

        //gportal 路由插件
        API api = buildAPI(list("gw1"), "apiName", list("host1"), list("/any"),
                           list("GET"), "svc",
                           list("{\"type\":\"0\",\"list\":[\"127.0.0.1\"],\"kind\":\"ip-restriction\"}"),
                           "HTTP",
                           Arrays.asList(buildProxyService("www.hango.com", "STATIC", 100, 80)),
                           null,
                           UriMatch.exact);

        mockConfigManager.updateConfig(api);
        Assert.assertEquals(2, mockK8sConfigStore.size());
        mockConfigManager.deleteConfig(api);
        Assert.assertEquals(0, mockK8sConfigStore.size());

        mockK8sConfigStore.clear();
    }

    @Test
    public void testUpdateService() {

        //动态
        Service service = buildProxyService(
                "httpbin.default.svc.cluster.local", "DYNAMIC", 100, 80,
                "gw1", "HTTP1", "SVC1", "serviceTag1");

        mockConfigManager.updateConfig(service);
        Assert.assertEquals(1, mockK8sConfigStore.size());

        mockConfigManager.deleteConfig(service);
        Assert.assertEquals(0, mockK8sConfigStore.size());


        //静态
        Service service1 = buildProxyService(
                "www.baidu.com", "STATIC", 100, 80,
                "gw1", "HTTP1", "SVC1", "serviceTag1");

        mockConfigManager.updateConfig(service1);
        // service entry + destination rule
        Assert.assertEquals(2, mockK8sConfigStore.size());

        mockConfigManager.deleteConfig(service1);
        Assert.assertEquals(0, mockK8sConfigStore.size());
    }

    @Test
    public void testUpdateGlobalPlugin() {

        GlobalPlugin gp1 = buildGlobalPlugin("demo", Arrays.asList("a", "b"), Arrays.asList("{\"limit_percent\":\"50\",\"kind\":\"percent-limit\"}"), "ok");
        mockConfigManager.updateConfig(gp1);
        Assert.assertEquals(1, mockK8sConfigStore.size());
        mockConfigManager.deleteConfig(gp1);
        Assert.assertEquals(0, mockK8sConfigStore.size());
    }


    private List<String> list(String s) {
        List<String> l = new ArrayList();
        l.add(s);
        return l;
    }

    private API buildAPI(List<String> gateways, String name, List<String> hosts, List<String> requestUris,
                         List<String> methods, String service, List<String> plugins, String protocol,
                         List<Service> proxyServices, List<String> proxyUris, UriMatch uriMatch) {

        API api = new API();
        api.setGateways(gateways);
        api.setName(name);
        api.setHosts(hosts);
        api.setRequestUris(requestUris);
        api.setMethods(methods);
        api.setService(service);
        api.setPlugins(plugins);
        api.setProtocol(protocol);
        api.setProxyServices(proxyServices);
        api.setProxyUris(proxyUris);
        api.setUriMatch(uriMatch);
        return api;
    }

    private Service buildProxyService(String backendService, String type, Integer weight, Integer port) {

        return buildProxyService(backendService, type, weight, port, null, null, null, null);
    }

    private Service buildProxyService(String backendService, String type, Integer weight, Integer port,
                                      String gateway, String protocol, String code, String serviceTag) {

        Service service = new Service();
        service.setBackendService(backendService);
        service.setType(type);
        service.setWeight(weight);
        service.setPort(port);
        service.setGateway(gateway);
        service.setProtocol(protocol);
        service.setCode(code);
        service.setServiceTag(serviceTag);
        return service;
    }

    private Endpoint buildEndpoint(String hostname, String address, Integer port) {

        Endpoint endpoint = new Endpoint();
        endpoint.setHostname(hostname);
        endpoint.setAddress(address);
        endpoint.setPort(port);
        return endpoint;
    }

    private Gateway buildGateway(String hostname, String address, Map<String, String> labels) {
        Gateway gateway = new Gateway();
        gateway.setHostname(hostname);
        gateway.setAddress(address);
        gateway.setLabels(labels);
        return gateway;
    }

    private GlobalPlugin buildGlobalPlugin(String gateway, List<String> hosts, List<String> plugins, String code) {
        GlobalPlugin gp = new GlobalPlugin();
        gp.setGateway(gateway);
        gp.setHosts(hosts);
        gp.setPlugins(plugins);
        gp.setCode(code);
        return gp;
    }

}