package org.hango.cloud.core.envoy;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.meta.dto.GatewayPluginDTO;
import org.hango.cloud.meta.dto.PluginOrderDTO;
import org.hango.cloud.meta.dto.PortalAPIDTO;
import org.hango.cloud.meta.dto.PortalServiceDTO;
import org.hango.cloud.service.GatewayService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class EnvoyHttpClientTest extends BaseTest {

    @Autowired
    EnvoyHttpClient envoyHttpClient;

    @MockBean(name = "restTemplate")
    RestTemplate restTemplate;

    @MockBean
    KubernetesClient k8sClient;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void getServiceHealth() {

        String resp = "{\"cluster_statuses\":[{\"name\":\"xds_cluster\",\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.108.238.209\",\"port_value\":15010}},\"stats\":[{\"value\":\"2\",\"name\":\"cx_connect_fail\"},{\"value\":\"427\",\"name\":\"cx_total\"},{\"value\":\"427\",\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"value\":\"426\",\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"value\":\"1\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"value\":\"1\",\"name\":\"rq_active\"}],\"health_status\":{\"failed_active_hc\":false,\"eds_health_status\":\"HEALTHY\"},\"weight\":1,\"hostname\":\"istio-pilot.istio-system.svc.cluster.local\"}]},{\"name\":\"outbound|9901||istio-galley.istio-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.65\",\"port_value\":9901}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"failed_eds_health\":true,\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]},{\"name\":\"outbound|9379||gateway-prometheus.gateway-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.6\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1},{\"address\":{\"socket_address\":{\"address\":\"10.244.2.6\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]},{\"name\":\"outbound|9379|sb1|gateway-prometheus.gateway-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.6\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]}]}";
        when(restTemplate.getForObject(anyString(), any())).thenReturn(resp);
        when(k8sClient.getObjectList(any(), any(), any())).thenReturn(Arrays.asList(getPod(null, getPodStatus("1.1.1.1", "Running"))));

        List<ServiceHealth> serviceHealth = envoyHttpClient.getServiceHealth(null, null, "gw1");

        Assert.assertEquals(4, serviceHealth.size());
    }

    @Test
    public void testYaml() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        String str = "{\"Code\":\"DYNAMIC-5756\",\"BackendService\":\"a.powerful.svc.cluster.local\",\"Type\":\"DYNAMIC\",\"Gateway\":\"demo-gateway\",\"Protocol\":\"http\",\"TrafficPolicy\":{\"LoadBalancer\":{\"Simple\":\"ROUND_ROBIN\",\"ConsistentHash\":null},\"HealthCheck\":{\"Path\":\"/healtch\",\"Timeout\":100,\"ExpectedStatuses\":[200],\"HealthyInterval\":50,\"HealthyThreshold\":2,\"UnhealthyInterval\":30,\"UnhealthyThreshold\":3},\"OutlierDetection\":null,\"ConnectionPool\":{\"TCP\":{\"MaxConnections\":1024,\"ConnectTimeout\":60000},\"HTTP\":{\"Http1MaxPendingRequests\":1024,\"Http2MaxRequests\":1024,\"MaxRequestsPerConnection\":0,\"IdleTimeout\":3000}}},\"ServiceTag\":\"healthtest\",\"Subsets\":[]}";
        PortalServiceDTO s1 = mapper.readValue(str, PortalServiceDTO.class);
        gatewayService.updateService(s1);
    }

    private Pod getPod(PodSpec spec, PodStatus status) {
        Pod pod = new Pod();
        pod.setSpec(spec);
        pod.setStatus(status);
        return pod;
    }

    private PodStatus getPodStatus(String ip, String phase) {
        PodStatus ps = new PodStatus();
        ps.setPodIP(ip);
        ps.setPhase(phase);
        return ps;
    }

    @Test
    public void pluginManager() throws Exception{
        String str = "{\n" +
                "          \"GatewayLabels\": {\n" +
                "              \"gw_cluster\": \"prod-gateway\"\n" +
                "          },\n" +
                "          \"Plugins\": [\n" +
                "            {\n" +
                "                  \"name\": \"com.netease.metadatahub\",\n" +
                "                  \"enable\": \"true\",\n" +
                "                  \"listenerType\": 2,\n" +
                "                  \"inline\":{\n" +
                "\"settings\": {\n" +
                "                      \"set_to_metadata\": [\n" +
                "                        {\n" +
                "                          \"name\": \":path\",\n" +
                "                          \"rename\": \"x-envoy-origin-path\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                          \"name\": \":method\",\n" +
                "                          \"rename\": \"x-envoy-origin-method\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                          \"name\": \":authority\",\n" +
                "                          \"rename\": \"x-envoy-origin-host\"\n" +
                "                        }\n" +
                "                      ]\n" +
                "                  }\n" +
                "                  }\n" +
                "                  \n" +
                "              }\n" +
                "          ]\n" +
                "        }";
        PluginOrderDTO pluginOrderDTO = JSONObject.parseObject(str, PluginOrderDTO.class);
        System.out.println(objectMapper.writeValueAsString(pluginOrderDTO));
        gatewayService.updatePluginOrder(pluginOrderDTO);
    }


    @Test
    public void envoyplugin() throws  Exception{
        String str = "{\"Hosts\":[\"gateway-proxy.qa-yl.service.163.org\",\"istio.com\",\"test.cn\",\"xty.com\",\"abc.abc\"],\"Gateway\":\"prod-gateway\",\"Code\":\"project2-3-1-cors\",\"PluginType\":\"cors\",\"Plugins\":[\"{\\\"maxAge\\\":false,\\\"kind\\\":\\\"cors\\\",\\\"corsPolicy\\\":{\\\"kind\\\":\\\"cors\\\",\\\"allowOriginRegex\\\":[\\\"openzfw.com\\\"]}}\"]}";
        String str1 = "{\"Hosts\":[\"gateway-proxy.qa-yl.service.163.org\",\"istio.com\",\"test.cn\",\"xty.com\",\"abc.abc\"],\"Gateway\":\"prod-gateway\",\"RouteId\":9779,\"PluginType\":\"ianus-router\",\"Plugins\":[\"{\\\"kind\\\":\\\"ianus-router\\\",\\\"rule\\\":[{\\\"name\\\":\\\"return\\\",\\\"action\\\":{\\\"rewrite_regex\\\":\\\"/(.*)\\\",\\\"action_type\\\":\\\"return\\\",\\\"return_target\\\":{\\\"code\\\":\\\"344\\\",\\\"body\\\":\\\"aaaasda\\\"},\\\"target\\\":\\\"/$1\\\"}}],\\\"version\\\":\\\"1.0\\\"}\"]}";

        GatewayPluginDTO pluginOrderDTO = JSONObject.parseObject(str1, GatewayPluginDTO.class);
        String str2 = "{\"kind\":\"ianus-router\",\"code\":\"344\",\"body\":\"aaaasda\",\"version\":\"1.0\"}";
        pluginOrderDTO.setPlugins(Arrays.asList(str2));
        String s = JSONObject.toJSONString(pluginOrderDTO);
        System.out.println(s);
        gatewayService.updateGatewayPlugin(pluginOrderDTO);
    }
    @Test
    public void testApi(){
        String str = "{\"Order\":50200861,\"ProxyServices\":[{\"BackendService\":\"istio-e2e-app.apigw-demo.svc.cluster.local\",\"Type\":\"DYNAMIC\",\"Port\":80,\"Code\":\"DYNAMIC-7134\",\"Weight\":100}],\"Hosts\":[\"gateway-proxy.qa-yl.service.163.org\"],\"RequestUris\":[\"/corsScenario\"],\"StatsMeta\":[10067],\"ServiceTag\":\"CorsScenarioServiceName\",\"Timeout\":60000,\"ProjectId\":3,\"RouteId\":10067,\"HttpRetry\":{\"Attempts\":2,\"RetryOn\":\"\",\"IsRetry\":false,\"PerTryTimeout\":60000},\"Code\":\"10067\",\"Gateway\":\"prod-gateway\",\"RouteName\":\"CorsScenario-RuleName-1\",\"UriMatch\":\"prefix\",\"Methods\":[\"*\"],\"Plugins\":[]}";
        PortalAPIDTO portalAPIDTO = JSONObject.parseObject(str, PortalAPIDTO.class);
        gatewayService.updateAPI(portalAPIDTO);
    }

}
