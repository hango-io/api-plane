package org.hango.cloud.core.gateway.service.impl;

import org.hango.cloud.core.envoy.EnvoyHttpClient;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.istio.PilotHttpClient;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.Gateway;
import org.hango.cloud.meta.HealthServiceSubset;
import org.hango.cloud.meta.ServiceAndPort;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/17
 **/
@Component
public class DefaultResourceManager implements ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceManager.class);

    @Autowired
    private PilotHttpClient istioHttpClient;

    @Autowired
    private EnvoyHttpClient envoyHttpClient;

    @Value("${service.namespace.exclude:gateway-system,kube-system,istio-system}")
    private String excludeNamespace;

    private List<String> getExcludeNamespace() {
        List<String> ret = new ArrayList<>();
        if (StringUtils.isEmpty(excludeNamespace)) return ret;
        ret.addAll(Arrays.asList(excludeNamespace.split(",")));
        return ret;
    }

    @Override
    public List<Endpoint> getEndpointList() {
        Predicate<Endpoint> filter = endpoint ->
                endpoint.getHostname() != null &&
                        endpoint.getHostname() != null &&
                        endpoint.getPort() != null;
        for (String ns : getExcludeNamespace()) {
            filter = filter.and(endpoint -> !inNamespace(endpoint.getHostname(), ns));
        }

        return istioHttpClient.getEndpointList(filter);
    }

    @Override
    public List<Gateway> getGatewayList() {
        return istioHttpClient.getGatewayList(gateway ->
                // 过滤静态服务
                !isServiceEntry(gateway.getHostname()) &&
                // 包含gw_cluster label
                Objects.nonNull(gateway.getLabels()) &&
                        gateway.getLabels().containsKey("gw_cluster"));
    }

    @Override
    public List<String> getServiceList() {
        Predicate<Endpoint> filter = endpoint ->
                endpoint.getHostname() != null &&
                        !isServiceEntry(endpoint.getHostname());
        for (String ns : getExcludeNamespace()) {
            filter = filter.and(endpoint -> !inNamespace(endpoint.getHostname(), ns));
        }
        return istioHttpClient.getServiceList(filter);
    }


    public List<ServiceAndPort> getServiceAndPortList() {
        Map<String, Set<Integer>> servicePortMap = new LinkedHashMap<>();
        getEndpointList().forEach(endpoint -> {
                    if (!servicePortMap.containsKey(endpoint.getHostname())) {
                        servicePortMap.put(endpoint.getHostname(), new LinkedHashSet<>());
                    }
                    servicePortMap.get(endpoint.getHostname()).add(endpoint.getPort());
                }
        );
        return servicePortMap.entrySet().stream()
                .filter(entry -> !isServiceEntry(entry.getKey()))
                .map(entry -> {
                    ServiceAndPort sap = new ServiceAndPort();
                    sap.setName(entry.getKey());
                    sap.setPort(new ArrayList<>(entry.getValue()));
                    return sap;
                }).collect(Collectors.toList());
    }

    @Override
    public Integer getServicePort(List<Endpoint> endpoints, String targetHost) {
        if (CollectionUtils.isEmpty(endpoints) || StringUtils.isBlank(targetHost)) {
            throw new ApiPlaneException("Get port by targetHost fail. param cant be null.");
        }
        List<Integer> ports = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            if (targetHost.equals(endpoint.getHostname())) {
                ports.add(endpoint.getPort());
            }
        }
        if (ports.size() == 0) {
            throw new ApiPlaneException(String.format("Target endpoint %s does not exist", targetHost));
        }
        //todo: ports.size() > 1
        return ports.get(0);
    }

    @Override
    public List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway) {
        final List<String> ss = subsets;
        List<ServiceHealth> serviceHealth = envoyHttpClient.getServiceHealth(
                name -> {
                    if (!name.contains("|")) return new HealthServiceSubset(name);
                    // 截取到第二个|
                    // e.g. outbound|9901|subset1|istio-galley.istio-system.svc.cluster.local -> subset1
                    String subset = name.substring(CommonUtil.xIndexOf(name, "|", 1) + 1, name.lastIndexOf("|"));
                    String h = name.substring(name.lastIndexOf("|")  + 1);
                    return new HealthServiceSubset(h, subset);
                },
                serviceSubset -> {
                    for (String subset : ss) {
                        if (Objects.equals(serviceSubset.getHost(), host)
                                && Objects.equals(serviceSubset.getSubset(), subset)) return true;
                    }
                    return false;
                }, gateway);

        return serviceHealth;
    }

    private boolean inNamespace(String hostName, String namespace) {
        String[] segments = StringUtils.split(hostName, ".");
        if (ArrayUtils.getLength(segments) != 5) return false;
        return Objects.equals(segments[1], namespace);
    }

    private boolean isServiceEntry(String hostname) {
        return StringUtils.contains(hostname, "com.netease.static");
    }



}
