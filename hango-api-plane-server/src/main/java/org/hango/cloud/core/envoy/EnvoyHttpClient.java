package org.hango.cloud.core.envoy;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.EndpointHealth;
import org.hango.cloud.meta.HealthServiceSubset;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EnvoyHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyHttpClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KubernetesClient client;

    @Value(value = "${gatewayNamespace:gateway-system}")
    private String gatewayNamespace;

    @Value(value = "${gatewayName:gateway-proxy}")
    private String gatewayName;

    @Value(value = "${envoyUrl:#{null}}")
    private String envoyUrl;

    private static final String GET_CLUSTER_HEALTH_JSON = "/clusters?format=json";

    private static final String SUBSET_PATTERN = ".+\\|\\d+\\|.+\\|.*";

    private static final String HEALTHY = "HEALTHY";
    private static final String UNHEALTHY = "UNHEALTHY";

    private String getEnvoyUrl() {
        return getEnvoyUrlByLabels(ImmutableMap.of("app", gatewayName));
    }

    private String getEnvoyUrl(String gateway) {
        return getEnvoyUrlByLabels(ImmutableMap.of("gw_cluster", gateway));
    }

    private String getEnvoyUrlByLabels(Map<String, String> labels) {
        if (!StringUtils.isEmpty(envoyUrl)) return envoyUrl;
        //envoy service暂时未暴露管理端口，直接拿pod ip
        List<Pod> envoyPods = client.getObjectList(K8sResourceEnum.Pod.name(), gatewayNamespace, labels);
        if (CollectionUtils.isEmpty(envoyPods)) throw new ApiPlaneException(ExceptionConst.ENVOY_POD_NON_EXIST);
        Optional<String> healthPod = envoyPods.stream()
                .filter(e -> e.getStatus().getPhase().equals("Running"))
                .map(e -> "http://" + e.getStatus().getPodIP())
                .findFirst();
        if (!healthPod.isPresent()) throw new ApiPlaneException(ExceptionConst.ENVOY_POD_NON_EXIST);
        //fixed port
        int port = 19000;
        return healthPod.get() + ":" + port;
    }

    public List<ServiceHealth> getServiceHealth(Function<String, HealthServiceSubset> nameHandler, Predicate<HealthServiceSubset> filter, String gateway) {

        Map<String, List<EndpointHealth>> healthMap = new HashMap<>();
        String resp = restTemplate.getForObject(getEnvoyUrl(gateway) + GET_CLUSTER_HEALTH_JSON, String.class);
        ResourceGenerator rg = ResourceGenerator.newInstance(resp, ResourceType.JSON);

        List endpoints = rg.getValue("$.cluster_statuses[?(@.host_statuses)]");
        Function<String, HealthServiceSubset> nameFunction = nameHandler == null ? n -> new HealthServiceSubset(n) : nameHandler;
        Predicate serviceFilter = filter == null ? n -> true : filter;
        if (CollectionUtils.isEmpty(endpoints)) return Collections.emptyList();
        endpoints.stream()
                .forEach(e -> {
                    ResourceGenerator gen = ResourceGenerator.newInstance(e, ResourceType.OBJECT);

                    String serviceName = gen.getValue("$.name");
                    //不同端口的服务算一个服务，以后缀为服务名
                    HealthServiceSubset serviceSubset = nameFunction.apply(serviceName);
                    if (!serviceFilter.test(serviceSubset)) return;
                    List<String> addrs = gen.getValue("$..socket_address.address");
                    List<Integer> ports = gen.getValue("$..socket_address.port_value");

                    if (CollectionUtils.isEmpty(addrs) || CollectionUtils.isEmpty(ports)) return;
                    if (addrs.size() != ports.size()) {
                        logger.warn("address size can not match port size");
                        return;
                    }
                    for (int i = 0; i < addrs.size(); i++) {
                        EndpointHealth eh = new EndpointHealth();
                        eh.setAddress(addrs.get(i) + ":" + ports.get(i));
                        eh.setStatus(healthStatus(gen, i));
                        eh.setSubset(serviceSubset.getSubset());
                        healthMap.computeIfAbsent(serviceSubset.getHost(), v -> new ArrayList()).add(eh);
                    }
                });

        List<ServiceHealth> shs = new ArrayList<>();
        healthMap.forEach((name, ehs) -> {
            ServiceHealth sh = new ServiceHealth();
            Map<EndpointHealth, String> mergedEhs = new HashMap<>();
            ehs.stream().forEach(eh -> {
                if (mergedEhs.containsKey(eh)) {
                    // 有任何一个实例不健康，则整体不健康
                    if (Objects.equals(eh.getStatus(), UNHEALTHY)
                            || Objects.equals(mergedEhs.get(eh), UNHEALTHY)) {
                        mergedEhs.put(eh, UNHEALTHY);
                    }
                } else {
                    mergedEhs.put(eh, eh.getStatus());
                }
            });
            sh.setName(name);
            sh.setEps(
                    mergedEhs.entrySet().stream()
                            .map(e -> {
                                e.getKey().setStatus(e.getValue());
                                return e.getKey(); })
                            .collect(Collectors.toList())
            );
            shs.add(sh);
        });
        return shs;
    }

    private String healthStatus(ResourceGenerator gen, int index) {
        Boolean failedActive = gen.getValue(String.format("$.host_statuses[%d].health_status.failed_active_health_check", index));
        Boolean failedOutlier = gen.getValue(String.format("$.host_statuses[%d].health_status.failed_outlier_check", index));

        boolean active = failedActive == null ? false : true;
        boolean outlier = failedOutlier == null ? false : true;

        return !(active || outlier) ? HEALTHY : UNHEALTHY;
    }

    private boolean isSubset(String name) {
        return Pattern.matches(SUBSET_PATTERN, name);
    }

}

