package org.hango.cloud.core.envoy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.ServiceHealth;
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


}
