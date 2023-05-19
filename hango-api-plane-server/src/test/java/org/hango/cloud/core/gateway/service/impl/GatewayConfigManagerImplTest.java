//package com.netease.cloud.nsf.core.gateway.service.impl;
//
//import com.google.common.collect.ImmutableMap;
//import com.netease.cloud.nsf.core.BaseTest;
//import com.netease.cloud.nsf.core.GlobalConfig;
//import com.netease.cloud.nsf.core.gateway.GatewayIstioModelEngine;
//import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
//import com.netease.cloud.nsf.meta.*;
//import com.netease.cloud.nsf.mock.MockEventPublisher;
//import com.netease.cloud.nsf.mock.MockK8sConfigStore;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.Mockito.when;
//
//
//public class GatewayConfigManagerImplTest extends BaseTest {
//
//    @Autowired
//    GatewayIstioModelEngine modelEngine;
//
//    @MockBean
//    ResourceManager resourceManager;
//
//    @Autowired
//    GlobalConfig globalConfig;
//
//    GatewayConfigManagerImpl mockConfigManager;
//    MockK8sConfigStore mockK8sConfigStore;
//
//    List<Endpoint> fixedEndpoints = Arrays.asList(buildEndpoint("a.default", "www.testa.com", 80),
//            buildEndpoint("b.default", "www.testb.com", 80));
//
//    List<Gateway> fixedGateways = Arrays.asList(buildGateway("demo.gateway-system.svc.cluster.local", "demo",
//            ImmutableMap.of("gw_cluster", "demo")));
//
//
//    @Before
//    public void before() {
//        when(resourceManager.getEndpointList()).thenReturn(fixedEndpoints);
//        when(resourceManager.getGatewayList()).thenReturn(fixedGateways);
//
//        mockK8sConfigStore = new MockK8sConfigStore();
//        mockConfigManager = new GatewayConfigManagerImpl(modelEngine, mockK8sConfigStore, globalConfig, new MockEventPublisher());
//    }
//
//    @Test
//    public void testUpdateAPI() {
//
//        //gportal 路由插件
//        API api = buildAPI(list("gw1"), "apiName", list("host1"), list("/any"),
//                list("GET"), "svc",
//                list("{\"kind\":\"ianus-router\",\"rule\":[{\"name\":\"rewrite\",\"matcher\":[{\"source_type\":\"Header\",\"left_value\":\"plugin\",\"op\":\"=\",\"right_value\":\"rewrite\"}],\"action\":{\"action_type\":\"rewrite\",\"rewrite_regex\":\"/rewrite/{group1}/{group2}\",\"target\":\"/anything/{{group2}}/{{group1}}\"}}]}"),
//                "HTTP",
//                Arrays.asList(buildProxyService("www.163.com", "STATIC", 100, 80)),
//                null,
//                UriMatch.exact);
//
//        mockConfigManager.updateConfig(api);
//        Assert.assertEquals(1, mockK8sConfigStore.size());
//        mockConfigManager.deleteConfig(api);
//        Assert.assertEquals(0, mockK8sConfigStore.size());
//
//        // 去除yx分支后，需要有ServiceProxy
//        List<Service> serviceList = new ArrayList<>();
//        Service service = new Service();
//        service.setCode("dynamic-5013");
//        service.setBackendService("istio-e2e-app.apigw-demo.svc.cluster.local");
//        service.setType("DYNAMIC");
//        service.setWeight(100);
//        service.setPort(80);
//        serviceList.add(service);
//        API api1 = buildAPI(list("gw1"), "apiName", list("host1"), list("/any"),
//                list("GET"), "svc",
//                list("{\"kind\":\"ianus-router\",\"rule\":[{\"name\":\"rewrite\",\"matcher\":[{\"source_type\":\"Header\",\"left_value\":\"plugin\",\"op\":\"=\",\"right_value\":\"rewrite\"}],\"action\":{\"action_type\":\"rewrite\",\"rewrite_regex\":\"/rewrite/{group1}/{group2}\",\"target\":\"/anything/{{group2}}/{{group1}}\"}}]}"),
//                "HTTP",
//                serviceList,
//                Arrays.asList("a.default", "b.default"),
//                UriMatch.exact);
//
//        mockConfigManager.updateConfig(api1);
//        Assert.assertEquals(1, mockK8sConfigStore.size());
//        mockConfigManager.deleteConfig(api1);
//        //保留gateway
//        Assert.assertEquals(0, mockK8sConfigStore.size());
//
//        mockK8sConfigStore.clear();
//    }
//
//    @Test
//    public void testUpdateService() {
//
//        //动态
//        Service service = buildProxyService(
//                "httpbin.default.svc.cluster.local", "DYNAMIC", 100, 80,
//                "gw1", "HTTP1", "SVC1", "serviceTag1");
//
//        mockConfigManager.updateConfig(service);
//        Assert.assertEquals(1, mockK8sConfigStore.size());
//
//        mockConfigManager.deleteConfig(service);
//        Assert.assertEquals(0, mockK8sConfigStore.size());
//
//
//        //静态
//        Service service1 = buildProxyService(
//                "www.baidu.com", "STATIC", 100, 80,
//                "gw1", "HTTP1", "SVC1", "serviceTag1");
//
//        mockConfigManager.updateConfig(service1);
//        // service entry + destination rule
//        Assert.assertEquals(2, mockK8sConfigStore.size());
//
//        mockConfigManager.deleteConfig(service1);
//        Assert.assertEquals(0, mockK8sConfigStore.size());
//    }
//
//    @Test
//    public void testUpdateGlobalPlugin() {
//        GatewayPlugin gp1 = buildGatewayPlugin("demo", Arrays.asList("a", "b"), Arrays.asList("{\"kind\":\"jsonp\",\"callback\":\"ddd\"}"), "ok");
//        mockConfigManager.updateConfig(gp1);
//        Assert.assertEquals(1, mockK8sConfigStore.size());
//        gp1.setPlugins(new ArrayList<>());
//        mockConfigManager.updateConfig(gp1);
//        Assert.assertEquals(0, mockK8sConfigStore.size());
//    }
//
//
//    private List<String> list(String s) {
//        List<String> l = new ArrayList();
//        l.add(s);
//        return l;
//    }
//
//    private API buildAPI(List<String> gateways, String name, List<String> hosts, List<String> requestUris,
//                         List<String> methods, String service, List<String> plugins, String protocol,
//                         List<Service> proxyServices, List<String> proxyUris, UriMatch uriMatch) {
//
//        API api = new API();
//        api.setGateways(gateways);
//        api.setName(name);
//        api.setHosts(hosts);
//        api.setRequestUris(requestUris);
//        api.setMethods(methods);
//        api.setService(service);
//        api.setPlugins(plugins);
//        api.setProtocol(protocol);
//        api.setProxyServices(proxyServices);
//        api.setProxyUris(proxyUris);
//        api.setUriMatch(uriMatch);
//        api.setApiId(8888L);
//        return api;
//    }
//
//    private Service buildProxyService(String backendService, String type, Integer weight, Integer port) {
//
//        return buildProxyService(backendService, type, weight, port, null, null, null, null);
//    }
//
//    private Service buildProxyService(String backendService, String type, Integer weight, Integer port,
//                                      String gateway, String protocol, String code, String serviceTag) {
//
//        Service service = new Service();
//        service.setBackendService(backendService);
//        service.setType(type);
//        service.setWeight(weight);
//        service.setPort(port);
//        service.setGateway(gateway);
//        service.setProtocol(protocol);
//        service.setCode(code);
//        service.setServiceTag(serviceTag);
//        return service;
//    }
//
//    private Endpoint buildEndpoint(String hostname, String address, Integer port) {
//
//        Endpoint endpoint = new Endpoint();
//        endpoint.setHostname(hostname);
//        endpoint.setAddress(address);
//        endpoint.setPort(port);
//        return endpoint;
//    }
//
//    private Gateway buildGateway(String hostname, String address, Map<String, String> labels) {
//        Gateway gateway = new Gateway();
//        gateway.setHostname(hostname);
//        gateway.setAddress(address);
//        gateway.setLabels(labels);
//        return gateway;
//    }
//
//    private GatewayPlugin buildGatewayPlugin(String gateway, List<String> hosts, List<String> plugins, String code) {
//        GatewayPlugin gp = new GatewayPlugin();
//        gp.setGateway(gateway);
//        gp.setHosts(hosts);
//        gp.setPlugins(plugins);
//        gp.setCode(code);
//        return gp;
//    }
//
//}