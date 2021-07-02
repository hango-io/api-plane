package org.hango.cloud.core.gateway;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.Trans;
import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.istio.PilotHttpClient;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.GlobalPlugin;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.slime.v1alpha1.EnvoyPlugin;
import me.snowdrop.istio.slime.v1alpha1.EnvoyPluginSpec;
import me.snowdrop.istio.slime.v1alpha1.PluginManager;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class IstioModelEngineTest extends BaseTest {

    @Autowired
    GatewayIstioModelEngine gatewayIstioModelEngine;

    @Autowired
    EditorContext editorContext;

    @MockBean
    KubernetesClient kubernetesClient;

    @MockBean
    PilotHttpClient istioHttpClient;

    private Endpoint getEndpoint(String hostname, int port) {
        Endpoint ep = new Endpoint();
        ep.setHostname(hostname);
        ep.setPort(port);
        return ep;
    }

    private PairMatch getPairMatch(String k, String v, String type) {
        PairMatch pm = new PairMatch();
        pm.setKey(k);
        pm.setValue(v);
        pm.setType(type);
        return pm;
    }

    private API getAPI(String name, String service, List<String> gateways, List<String> hosts,
                       List<String> uris, List<String> methods, List<String> proxies, UriMatch uriMatch,
                       List<Service> proxyServices, List<PairMatch> headers, List<PairMatch> queryParams) {
        API api = new API();
        api.setGateways(gateways);
        api.setName(name);
        api.setHosts(hosts);
        api.setRequestUris(uris);
        api.setMethods(methods);
        api.setProxyUris(proxies);
        api.setProxyServices(proxyServices);
        api.setService(service);
        api.setUriMatch(uriMatch);
        api.setHeaders(headers);
        api.setQueryParams(queryParams);
        return api;
    }

    private API setAPIVirtualCluster(API api,  String virtualClusterName, List<PairMatch> virtualClusterHeaders){
        api.setVirtualClusterName(virtualClusterName);
        api.setVirtualClusterHeaders(virtualClusterHeaders);
        return api;
    }

    private Service getService(String type, String backend, int weight, String code, String gateway, String protocol, String serviceTag) {
        Service s = new Service();
        s.setType(type);
        s.setBackendService(backend);
        s.setWeight(weight);
        s.setCode(code);
        s.setGateway(gateway);
        s.setProtocol(protocol);
        s.setServiceTag(serviceTag);
        return s;
    }

    @Test
    public void testTranslateAPI() {

        Endpoint endpoint1 = getEndpoint("a.default.svc.cluster.local", 9090);
        Endpoint endpoint2 = getEndpoint("b.default.svc.cluster.local", 9000);

        when(istioHttpClient.getEndpointList(any())).thenReturn(Arrays.asList(endpoint1, endpoint2));

        //base api test
        API api = getAPI("api-name", "service-zero", ImmutableList.of("gateway1", "gateway2"),
                ImmutableList.of("service-a", "service-b"), ImmutableList.of("/a", "/b"), ImmutableList.of("HTTP", "POST"),
                ImmutableList.of("a.default.svc.cluster.local", "b.default.svc.cluster.local"), UriMatch.prefix, Arrays.asList(buildProxyService("www.163.com", "STATIC", 100, 80)),
                ImmutableList.of(getPairMatch("k1", "v1", "exact"), getPairMatch("k2", "v2", "regex")),
                ImmutableList.of(getPairMatch("k3", "v3", "prefix"), getPairMatch("k4", "v4", "regex")));

        List<K8sResourcePack> resources = gatewayIstioModelEngine.translate(api);

        Assert.assertTrue(resources.size() == 4);

        resources.stream()
                .map(r -> r.getResource())
                .forEach(r -> {
                    if (r.getKind().equals(VirtualService.class.getSimpleName())) {
                        VirtualService vs = (VirtualService) r;
                        HTTPRoute httpRoute = vs.getSpec().getHttp().get(0);
                        HTTPMatchRequest match = httpRoute.getMatch().get(0);

                        RegexMatchType methodMatch = (RegexMatchType) match.getMethod().getMatchType();
                        Assert.assertTrue(methodMatch.getRegex().equals("HTTP|POST"));

                        RegexMatchType uriMatch = (RegexMatchType) match.getUri().getMatchType();
                        Assert.assertTrue(uriMatch.getRegex().equals("/a.*|/b.*"));

                        Map<String, StringMatch> headers = match.getHeaders();
                        Assert.assertTrue(headers.containsKey("k1"));
                        Assert.assertTrue(headers.containsKey("k2"));

                        Map<String, StringMatch> queryParams = match.getQueryParams();
                        Assert.assertTrue(queryParams.containsKey("k3"));
                        Assert.assertTrue(queryParams.containsKey("k4"));
                    }
                });

        API api1 = getAPI("api-name", "default", ImmutableList.of("gateway1"),
                ImmutableList.of("service-a", "service-b"), ImmutableList.of("/a", "/b"),
                ImmutableList.of("HTTP", "POST"), Collections.EMPTY_LIST, UriMatch.regex, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        Service s1 = getService(Const.PROXY_SERVICE_TYPE_DYNAMIC, "a.default.svc.cluster.local",
                                33, "DYNAMIC_1", null, "https", "asvc");
        Service s2 = getService(Const.PROXY_SERVICE_TYPE_STATIC, "www.baidu.com",
                33, "STATIC_1", null, "https", "baidu");
        Service s3 = getService(Const.PROXY_SERVICE_TYPE_STATIC, "10.10.10.10:1024,10.10.10.9:1024",
                34, "STATIC_2", null, "https","static1");
        api1.setProxyServices(Arrays.asList(s1, s2, s3));

        List<K8sResourcePack> resources1 = gatewayIstioModelEngine.translate(api1);

        resources1.stream()
                .map(r -> r.getResource())
                .forEach(r -> {
                    Assert.assertFalse(r.getKind().equals(K8sResourceEnum.DestinationRule.name()));
                    if (r.getKind().equals(VirtualService.class.getSimpleName())) {
                        VirtualService vs = (VirtualService) r;
                        HTTPRoute httpRoute = vs.getSpec().getHttp().get(0);
                        Assert.assertTrue(httpRoute.getRoute().size() == 3);
                    }
                });


        //virtualCluster test
        //base api test
        API api2 = setAPIVirtualCluster(api, "test-vc", Lists.newArrayList());

        List<K8sResourcePack> resources2 = gatewayIstioModelEngine.translate(api2);

        Assert.assertTrue(resources2.size() == 4);

        resources2.stream()
                .map(r -> r.getResource())
                .forEach(r -> {
                    if (r.getKind().equals(VirtualService.class.getSimpleName())) {
                        VirtualService vs = (VirtualService) r;
                        VirtualCluster virtualCluster = vs.getSpec().getVirtualCluster().get(0);

                        Assert.assertTrue(virtualCluster.getName().equals("test-vc"));
                    }
                });
    }

    @Test
    public void testTranslatePluginManager() {

        PluginOrderDTO po = new PluginOrderDTO();
        po.setGatewayLabels(ImmutableMap.of("k1","v1", "k2", "v2"));
        po.setPlugins(ImmutableList.of(
                getPlugin("p1", true, null),
                getPlugin("p2", false, null),
                getPlugin("p3", true, ImmutableMap.of("key","good"))));

        List<K8sResourcePack> res = gatewayIstioModelEngine.translate(Trans.pluginOrderDTO2PluginOrder(po));

        Assert.assertTrue(res.size() == 1);

        PluginManager pm = (PluginManager) res.get(0).getResource();

        Assert.assertTrue(pm.getSpec().getWorkloadLabels().size() == 2);
        Assert.assertTrue(pm.getSpec().getPlugin().size() == 3);

        PluginOrderDTO po1 = new PluginOrderDTO();
        po1.setPlugins(ImmutableList.of(
                getPlugin("p1", false, null),
                getPlugin("p2", true, ImmutableMap.of("key","good"))));

        List<K8sResourcePack> res1 = gatewayIstioModelEngine.translate(Trans.pluginOrderDTO2PluginOrder(po1));

        Assert.assertTrue(res1.size() == 1);

        PluginManager pm1 = (PluginManager) res1.get(0).getResource();
        Assert.assertTrue(CollectionUtils.isEmpty(pm1.getSpec().getWorkloadLabels()));
        Assert.assertTrue(pm1.getMetadata().getName().equals("qz-global"));
        assertEquals(2, pm1.getSpec().getPlugin().size());
        assertEquals("p1", pm1.getSpec().getPlugin().get(0).getName());
        assertEquals(false, pm1.getSpec().getPlugin().get(0).getEnable());
        assertEquals("p2", pm1.getSpec().getPlugin().get(1).getName());
        assertEquals(true, pm1.getSpec().getPlugin().get(1).getEnable());
        assertEquals(ImmutableMap.of("key","good"), pm1.getSpec().getPlugin().get(1).getSettings());
    }

    @Test
    public void testTranslateService() {

        Service service = getService(Const.PROXY_SERVICE_TYPE_DYNAMIC, "a.svc.cluster", 100, "a", "gw1", "http", "asvc");

        List<K8sResourcePack> istioResources = gatewayIstioModelEngine.translate(service);

        Assert.assertTrue(istioResources.size() == 1);
        DestinationRule ds = (DestinationRule) istioResources.get(0).getResource();
        DestinationRuleSpec spec = ds.getSpec();
        Assert.assertTrue(spec.getHost().equals(service.getBackendService()));

        Service service1 = getService(Const.PROXY_SERVICE_TYPE_STATIC, "10.10.10.10:1024,10.10.10.9:1025", 100, "b", "gw2", "https", "static-1");

        List<K8sResourcePack> istioResources1 = gatewayIstioModelEngine.translate(service1);
        Assert.assertTrue(istioResources1.size() == 2);
        istioResources1.stream()
                .map(r -> r.getResource())
                .forEach(ir -> {
                    if (ir.getKind().equals(K8sResourceEnum.DestinationRule.name())) {
                        DestinationRule ds1 = (DestinationRule) istioResources.get(0).getResource();
                        Assert.assertTrue(ds1.getSpec().getHost().equals(service.getBackendService()));
                    } else if (ir.getKind().equals(K8sResourceEnum.ServiceEntry.name())) {
                        ServiceEntry se = (ServiceEntry) ir;
                        Assert.assertTrue(se.getSpec().getEndpoints().size() == 2);
                        Assert.assertTrue(se.getSpec().getHosts().get(0).equals(decorateHost(service1.getCode())));
                    } else {
                        Assert.fail();
                    }
                });

    }

    String decorateHost(String code) {
        return String.format("com.netease.%s", code);
    }

    @Test
    public void subtractTest() {

        String vsYaml = "kind: VirtualService\n" +
                "metadata:\n" +
                "  creationTimestamp: 2019-08-20T12:41:10Z\n" +
                "  generation: 1\n" +
                "  labels:\n" +
                "    api_service: service-zero\n" +
                "  name: service-zero-gateway-yx\n" +
                "  namespace: gateway-system\n" +
                "  resourceVersion: \"30048280\"\n" +
                "  selfLink: /apis/networking.istio.io/v1alpha3/namespaces/gateway-system/virtualservices/service-zero-gateway-yx\n" +
                "  uid: c98eacf7-c347-11e9-8a87-fa163e5fcbdd\n" +
                "spec:\n" +
                "  gateways:\n" +
                "  - service-zero-gateway-yx\n" +
                "  hosts:\n" +
                "  - www.test.com\n" +
                "  http:\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        plugin:\n" +
                "          regex: rewrite\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      queryParams:\n" +
                "        plugin:\n" +
                "          regex: rewrite\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    name: plane-istio-test\n" +
                "    requestTransform:\n" +
                "      new:\n" +
                "        path: /{{backendUrl}}\n" +
                "      original:\n" +
                "        path: /rewrite/{backendUrl}\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        Cookie:\n" +
                "          regex: .*(?:;|^)plugin=r.*(?:;|$).*\n" +
                "        plugin:\n" +
                "          regex: redirect\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    name: plane-istio-test\n" +
                "    redirect:\n" +
                "      uri: /redirect\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        plugin:\n" +
                "          regex: return\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    name: plane-istio-test\n" +
                "    return:\n" +
                "      body:\n" +
                "        inlineString: '{is return plugin}'\n" +
                "      code: 403\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100\n" +
                "  - match:\n" +
                "    - method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    name: plane-istio-test\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 30\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 70\n" +
                "  - match:\n" +
                "    - method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: .*\n" +
                "    name: plane-istio-test-1\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100";

        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(vsYaml, ResourceType.YAML);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        IstioResource vs = (IstioResource) gen.object(resourceEnum.mappingType());

        VirtualService subtractedVs = (VirtualService) gatewayIstioModelEngine.subtract(vs,
                ImmutableMap.of(K8sResourceEnum.VirtualService.name(), "plane-istio-test"));

        Assert.assertTrue(subtractedVs.getSpec().getHttp().size() == 1);
    }

    private PluginOrderItemDTO getPlugin(String name, boolean enable, Object content) {
        PluginOrderItemDTO item = new PluginOrderItemDTO();
        item.setName(name);
        item.setEnable(enable);
        item.setSettings(content);
        return item;
    }

    @Test
    public void testTranslateGlobalPlugin() {

        GlobalPlugin gp1 = getGlobalPlugin("code1", Collections.EMPTY_LIST,
                                           "gateway-system/gw1", Arrays.asList("host1", "host2"));

        List<K8sResourcePack> resources = gatewayIstioModelEngine.translate(gp1);

        assertEquals(1, resources.size());

        EnvoyPlugin gatewayPlugin = (EnvoyPlugin) resources.get(0).getResource();
        EnvoyPluginSpec spec = gatewayPlugin.getSpec();
        assertEquals(2, spec.getHost().size());
        assertTrue(spec.getHost().containsAll(Arrays.asList("host1", "host2")));
        assertEquals("code1", gatewayPlugin.getMetadata().getName());
        assertEquals(1, spec.getWorkloadSelector().getLabels().size());
        assertEquals("gateway-system/gw1", spec.getWorkloadSelector().getLabels().get("gw_cluster"));
    }


    private GlobalPlugin getGlobalPlugin(String code, List<String> plugins, String gateway, List<String> hosts) {

        GlobalPlugin gp = new GlobalPlugin();
        gp.setCode(code);
        gp.setPlugins(plugins);
        gp.setGateway(gateway);
        gp.setHosts(hosts);
        return gp;
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
}
