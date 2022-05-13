package org.hango.cloud.core.gateway.service.impl;

import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.envoy.EnvoyHttpClient;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.ServiceHealth;
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

public class DefaultResourceManagerTest extends BaseTest {

    @Autowired
    ResourceManager resourceManager;

    @Autowired
    EnvoyHttpClient envoyHttpClient;

    @MockBean(name = "restTemplate")
    RestTemplate restTemplate;

    @MockBean
    KubernetesClient k8sClient;

    @Test
    public void getServiceHealthList() {

        String resp = "{\"cluster_statuses\":[{\"name\":\"xds_cluster\",\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.108.238.209\",\"port_value\":15010}},\"stats\":[{\"value\":\"2\",\"name\":\"cx_connect_fail\"},{\"value\":\"427\",\"name\":\"cx_total\"},{\"value\":\"427\",\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"value\":\"426\",\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"value\":\"1\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"value\":\"1\",\"name\":\"rq_active\"}],\"health_status\":{\"failed_active_hc\":false,\"eds_health_status\":\"HEALTHY\"},\"weight\":1,\"hostname\":\"istio-pilot.istio-system.svc.cluster.local\"}]},{\"name\":\"outbound|9901|subset1|istio-galley.istio-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.65\",\"port_value\":9901}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]},{\"name\":\"outbound|9901|subset2|istio-galley.istio-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.65\",\"port_value\":9901}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]},{\"name\":\"outbound|9379|sb1|gateway-prometheus.gateway-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.6\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1},{\"address\":{\"socket_address\":{\"address\":\"10.244.2.7\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"failed_active_health_check\":true,\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]},{\"name\":\"outbound|9379|sb3|gateway-prometheus.gateway-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.6\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1},{\"address\":{\"socket_address\":{\"address\":\"10.244.2.7\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]},{\"name\":\"outbound|9379|sb2|gateway-prometheus.gateway-system.svc.cluster.local\",\"added_via_api\":true,\"host_statuses\":[{\"address\":{\"socket_address\":{\"address\":\"10.244.2.6\",\"port_value\":9090}},\"stats\":[{\"name\":\"cx_connect_fail\"},{\"name\":\"cx_total\"},{\"name\":\"rq_error\"},{\"name\":\"rq_success\"},{\"name\":\"rq_timeout\"},{\"name\":\"rq_total\"},{\"type\":\"GAUGE\",\"name\":\"cx_active\"},{\"type\":\"GAUGE\",\"name\":\"rq_active\"}],\"health_status\":{\"failed_active_health_check\":true,\"eds_health_status\":\"HEALTHY\"},\"weight\":1}]}]}";
        when(restTemplate.getForObject(anyString(), any())).thenReturn(resp);
        when(k8sClient.getObjectList(any(), any(), any())).thenReturn(Arrays.asList(getPod(null, getPodStatus("1.1.1.1", "Running"))));

        String gateway = "gw1";
        String host1 = "istio-galley.istio-system.svc.cluster.local";
        List<String> subset1 = Arrays.asList("subset1", "subset2");
        String host2 = "gateway-prometheus.gateway-system.svc.cluster.local";
        List<String> subset2_1 = Arrays.asList("sb1", "sb3");
        List<String> subset2_2 = Arrays.asList("sb1", "sb2", "sb3");

        List<ServiceHealth> shs1 = resourceManager.getServiceHealthList(host1, subset1, gateway);
        Assert.assertEquals(1, shs1.size());
        Assert.assertEquals("HEALTHY", shs1.get(0).getEps().get(0).getStatus());

        List<ServiceHealth> shs2 = resourceManager.getServiceHealthList(host2, subset2_1, gateway);
        Assert.assertEquals(1, shs1.size());
        shs2.get(0).getEps()
                .forEach(ep -> {
                    if (ep.getAddress().equals("10.244.2.7")) {
                        Assert.assertEquals("UNHEALTHY", ep.getStatus());
                    } else if (ep.getAddress().equals("10.244.2.6")) {
                        Assert.assertEquals("HEALTHY", ep.getStatus());
                    }
                });

        List<ServiceHealth> shs3 = resourceManager.getServiceHealthList(host2, subset2_2, gateway);
        Assert.assertEquals(1, shs1.size());
        shs3.get(0).getEps()
                .forEach(ep -> {
                    if (ep.getAddress().equals("10.244.2.7")) {
                        Assert.assertEquals("UNHEALTHY", ep.getStatus());
                    } else if (ep.getAddress().equals("10.244.2.6")) {
                        Assert.assertEquals("UNHEALTHY", ep.getStatus());
                    }
                });
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
