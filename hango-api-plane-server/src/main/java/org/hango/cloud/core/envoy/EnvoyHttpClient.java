package org.hango.cloud.core.envoy;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sClient;
import org.hango.cloud.meta.EndpointHealth;
import org.hango.cloud.meta.HealthServiceSubset;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hango.cloud.util.exception.ExceptionConst.ENVOY_SERVICE_NON_EXIST;

@Component
public class EnvoyHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyHttpClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private K8sClient client;


    @Value(value = "${gatewayNamespace:gateway-system}")
    private String gatewayNamespace;

    @Value(value = "${gatewayName:gateway-proxy}")
    private String gatewayName;

    @Value(value = "${envoyUrl:#{null}}")
    private String envoyUrl;

    private static final String GET_CLUSTER_HEALTH_JSON = "/clusters?format=json";


    private static final String HEALTHY = "HEALTHY";
    private static final String UNHEALTHY = "UNHEALTHY";

    private static final String OPERATOR_IN = "In";
    private static final String OPERATOR_NOT_IN = "NotIn";
    private static final String OPERATOR_EXISTS = "Exists";
    private static final String OPERATOR_DOES_NOT_EXIST = "DoesNotExist";
    private static final String OPERATOR_GT = "Gt";
    private static final String OPERATOR_LT = "Lt";
    public static final String INTERNAL_IP = "InternalIP";


    /**
     * 获取envoy pod ip
     * @param gateway: gwClusterName
     * @return ip:19000
     */
    private String getEnvoyUrl(String gateway) {
        List<Service> envoyServiceList = getEnvoyServiceList(gateway);
        if (CollectionUtils.isEmpty(envoyServiceList)) {
            throw new ApiPlaneException(ExceptionConst.ENVOY_SERVICE_NON_EXIST);
        }
        return String.format("http://%s:19000", envoyServiceList.get(0).getSpec().getClusterIP());
    }

    /**
     * 获取envoy pod
     */
    public Pod getEnvoyPod(String gwClusterName){
        List<Pod> podList = client.getPods(gatewayNamespace, ImmutableMap.of("gw_cluster", gwClusterName));
        if (CollectionUtils.isEmpty(podList)) {
            throw new ApiPlaneException(ExceptionConst.ENVOY_POD_NON_EXIST);
        }
        Pod runningPod = podList.stream().filter(e -> e.getStatus().getPhase().equals("Running")).findFirst().orElse(null);
        if (runningPod == null){
            throw new ApiPlaneException(ExceptionConst.ENVOY_POD_NON_EXIST);
        }
        return runningPod;
    }

    /**
     * 获取envoy service
     * 1.基于gwclusterName获取 envoy pod
     * 2.从envoy pod 获取 app label
     * 3.基于app label 获取envoy service
     */
    public List<Service> getEnvoyServiceList(String gwClusterName){
        Pod envoyPod = getEnvoyPod(gwClusterName);
        String app = envoyPod.getMetadata().getLabels().get("app");
        if (StringUtils.isEmpty(app)){
            throw new ApiPlaneException(ENVOY_SERVICE_NON_EXIST);
        }
        return client.getServices(gatewayNamespace, ImmutableMap.of("app", gatewayName));
    }

    /**
     * 获取envoy可以调度的node ip，会基于nodeAffinity做node过滤
     * ex:
     * nodeAffinity:
     *   requiredDuringSchedulingIgnoredDuringExecution:
     *     nodeSelectorTerms:
     *     - matchExpressions:
     *       - key: skiff/apigw-gateway
     *         operator: In
     *         values:
     *         - "true"
     *
     * 匹配node label中skiff/apigw-gateway=true的节点
     */
    public List<String> getEnvoySchedulableNodeAddress(String gwClusterName) {
        List<Node> node = client.getNode();
        List<NodeSelectorRequirement> nodeSelectorTerm = getEnvoyNodeSelectorTerm(gwClusterName);
        return node.stream().filter(o -> isSchedulable(o, nodeSelectorTerm)).map(this::parseNodeAddress).collect(Collectors.toList());
    }

    private List<NodeSelectorRequirement> getEnvoyNodeSelectorTerm(String gwClusterName){
        Pod pod = getEnvoyPod(gwClusterName);
        Affinity affinity = pod.getSpec().getAffinity();
        if (affinity == null){
            return new ArrayList<>();
        }
        NodeAffinity nodeAffinity = affinity.getNodeAffinity();
        if (nodeAffinity == null){
            return new ArrayList<>();
        }
        NodeSelector requiredSelector = nodeAffinity.getRequiredDuringSchedulingIgnoredDuringExecution();
        if (requiredSelector == null){
            return new ArrayList<>();
        }
        return requiredSelector.getNodeSelectorTerms()
                .stream()
                .flatMap(term -> term.getMatchExpressions().stream())
                .collect(Collectors.toList());
    }

    /**
     * 基于k8s操作符匹配节点信息
     * 操作符包括：[In,NotIn,Exists,DoesNotExist,Gt,Lt]
     * @param node 节点
     * @param nodeSelectorRequirements nodeselector匹配条件
     * @return 是否允许调度
     */
    @SuppressWarnings("java:S3776")
    private boolean isSchedulable(Node node, List<NodeSelectorRequirement> nodeSelectorRequirements){
        for (NodeSelectorRequirement requirement : nodeSelectorRequirements) {
            String key = requirement.getKey();
            String operator = requirement.getOperator();
            List<String> values = requirement.getValues();

            Map<String, String> labels = node.getMetadata().getLabels();
            String nodeValue = labels.get(key);

            if (nodeValue == null) {
                return false;
            }
            switch (operator) {
                case OPERATOR_IN:
                    if (!values.contains(nodeValue)) {
                        return false;
                    }
                    break;
                case OPERATOR_NOT_IN:
                    if (values.contains(nodeValue)) {
                        return false;
                    }
                    break;
                case OPERATOR_EXISTS:
                    // 如果节点的标签值不为空，则符合要求
                    break;
                case OPERATOR_DOES_NOT_EXIST:
                    // 如果节点的标签值为空，则符合要求
                    return false;
                case OPERATOR_GT:
                    String max = Collections.max(values);
                    if (nodeValue.compareTo(max) <= 0) {
                        return false;
                    }
                    break;
                case OPERATOR_LT:
                    String min = Collections.min(values);
                    if (nodeValue.compareTo(min) >= 0) {
                        return false;
                    }
                    break;
                default:
                    // 如果操作符不是 In、NotIn、Exists、DoesNotExist、Gt 或 Lt，则不符合要求
                    return false;
            }
        }
        return true;
    }

    private String parseNodeAddress(Node node){
        List<NodeAddress> addresses = node.getStatus().getAddresses();
        if (CollectionUtils.isEmpty(addresses)){
            return Strings.EMPTY;
        }
        for (NodeAddress address : addresses) {
            if (address.getType().equals(INTERNAL_IP)){
                return address.getAddress();
            }
        }
        return Strings.EMPTY;
    }


    @SuppressWarnings("java:S3776")
    public List<ServiceHealth> getServiceHealth(Function<String, HealthServiceSubset> nameHandler, Predicate<HealthServiceSubset> filter, String gateway) {

        Map<String, List<EndpointHealth>> healthMap = new HashMap<>();
        String resp = restTemplate.getForObject(getEnvoyUrl(gateway) + GET_CLUSTER_HEALTH_JSON, String.class);
        ResourceGenerator rg = ResourceGenerator.newInstance(resp, ResourceType.JSON);

        List endpoints = rg.getValue("$.cluster_statuses[?(@.host_statuses)]");
        Function<String, HealthServiceSubset> nameFunction = nameHandler == null ? HealthServiceSubset::new : nameHandler;
        Predicate serviceFilter = filter == null ? n -> true : filter;
        if (CollectionUtils.isEmpty(endpoints)) return Collections.emptyList();
        endpoints.forEach(e -> {
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
                        healthMap.computeIfAbsent(serviceSubset.getHost(), v -> new ArrayList<>()).add(eh);
                    }
                });

        List<ServiceHealth> shs = new ArrayList<>();
        healthMap.forEach((name, ehs) -> {
            ServiceHealth sh = new ServiceHealth();
            Map<EndpointHealth, String> mergedEhs = new HashMap<>();
            ehs.forEach(eh -> {
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

        boolean active = failedActive != null;
        boolean outlier = failedOutlier != null;

        return !(active || outlier) ? HEALTHY : UNHEALTHY;
    }


}

