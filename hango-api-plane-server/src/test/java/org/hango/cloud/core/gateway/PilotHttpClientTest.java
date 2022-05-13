package org.hango.cloud.core.gateway;

import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.istio.PilotHttpClient;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.Endpoint;
import io.fabric8.kubernetes.api.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


public class PilotHttpClientTest extends BaseTest {

    @Autowired
    PilotHttpClient istioHttpClient;

    @MockBean(name = "restTemplate")
    RestTemplate restTemplate;

    @MockBean
    KubernetesClient k8sClient;

    @Test
    public void getEndpointList() {

        String resp = "rate-limit.gateway-system.svc.cluster.local:grpc 10.244.1.29:18081 app=rate-limit,pod-template-hash=6ffc9fdcf9 spiffe://cluster.local/ns/gateway-system/sa/default\n" +
                "rate-limit.gateway-system.svc.cluster.local:config-grpc 10.244.1.29:16071 app=rate-limit,pod-template-hash=6ffc9fdcf9 spiffe://cluster.local/ns/gateway-system/sa/default\n" +
                "ratings.default.svc.cluster.local:http 10.244.0.154:9080 app=ratings,pod-template-hash=7bdfd65ccc,version=v1 spiffe://cluster.local/ns/default/sa/bookinfo-ratings\n" +
                "redis.gateway-system.svc.cluster.local:redis 10.244.1.49:6379 app=redis,pod-template-hash=5cc57965f7 spiffe://cluster.local/ns/gateway-system/sa/default\n" +
                "reviews.default.svc.cluster.local:http 10.244.1.242:9080 app=reviews,pod-template-hash=844bc59d88,version=v3 spiffe://cluster.local/ns/default/sa/bookinfo-reviews\n" +
                "tiller-deploy.kube-system.svc.cluster.local:tiller 10.244.1.244:44134 app=helm,name=tiller,pod-template-hash=856685bc59 spiffe://cluster.local/ns/kube-system/sa/tiller\n";

        ResponseEntity entity = new ResponseEntity(resp, HttpStatus.OK);

        Service service = buildService("10.10.10.18", 8080);

        when(k8sClient.getObjectList(any(),any(),any())).thenReturn(Arrays.asList(service));
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(entity);

        List<Endpoint> endpoints = istioHttpClient.getEndpointList(f -> true);

        Assert.assertNotNull(endpoints);
        Assert.assertEquals(6, endpoints.size());
        Assert.assertEquals("rate-limit.gateway-system.svc.cluster.local", endpoints.get(0).getHostname());
        Assert.assertEquals("10.244.1.29", endpoints.get(0).getAddress());
        Assert.assertEquals(new Integer(18081), endpoints.get(0).getPort());
    }

    private Service buildService(String clusterIP, Integer port) {

        Service service = new Service();
        ServiceSpec spec = new ServiceSpec();
        spec.setClusterIP(clusterIP);
        spec.setPorts(Arrays.asList(buildServicePort(port)));
        service.setSpec(spec);
        return service;
    }

    private ServicePort buildServicePort(Integer port) {
        ServicePort servicePort = new ServicePort();
        servicePort.setPort(port);
        return servicePort;
    }
}
