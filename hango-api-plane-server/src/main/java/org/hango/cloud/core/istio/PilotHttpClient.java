package org.hango.cloud.core.istio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.Gateway;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Component
public class PilotHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(PilotHttpClient.class);

    @Value(value = "${istioNamespace:gateway-system}")
    private String NAMESPACE;

    @Value("#{ '${istioNamespaces:istio-system}'.split(',') }")
    private List<String> pilotNamespaces;

    @Value(value = "${istioName:istiod}")
    private String NAME;

//    private static final String GET_ENDPOINTZ_PATH = "/debug/endpointz?brief=true&instancePort=true";
    //todo 优化istio-apiplane处理逻辑
    private static final String GET_ENDPOINTZ_PATH = "/debug/endpointz?brief=true";
    private static final String GET_CONFIGZ_PATH = "/debug/configz";
    private static final String HEALTH_CHECK_PATH = "/ready";

    @Value(value = "${istioHttpUrl:#{null}}")
    private String istioHttpUrl;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    @Qualifier("shortTimeoutRestTemplate")
    RestTemplate shortTimeoutRestTemplate;

    @Autowired
    private KubernetesClient client;

    @Autowired
    ObjectMapper objectMapper;

    @Value(value = "${endpointExpired:10}")
    private Long endpointCacheExpired;


    LoadingCache<String, Object> endpointsCache;
    LoadingCache<String, Map<String, Map<String, String>>> statusCache;
    public static final String PILOT_GLOBAL_KEY = "global";

    @PostConstruct
    void cacheInit() {
        endpointsCache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .initialCapacity(1)
                .expireAfterWrite(endpointCacheExpired, TimeUnit.SECONDS)
                .recordStats()
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String key) throws Exception {
                        return getForEntity(getIstioUrl() + GET_ENDPOINTZ_PATH, String.class).getBody();
                    }
                });
    }

    @PostConstruct
    void statusCacheInit() {
        statusCache = CacheBuilder.newBuilder()
            .initialCapacity(1)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .recordStats()
            .build(new CacheLoader<String, Map<String, Map<String, String>>>() {
                @Override
                public Map<String, Map<String, String>> load(String key) throws IOException {
                    List<String> urls = new ArrayList<>();
                    if (PILOT_GLOBAL_KEY.equals(key)) {
                        urls = getIstioPodUrls();
                    } else if (key.startsWith("istio-env/")) {
                        urls = getUrls(key.replaceAll("^istio-env/", ""));
                    }
                    return urls.stream()
						.flatMap(istioUrl -> syncz(istioUrl).stream())
                        .collect(Collectors.toMap(m -> m.get("proxy"), m -> m, (m1, m2) -> {
                            logger.error("syncz pod conflict: {}", m1.get("proxy"));
                            return m1;
                        }));
                }
            });
    }

    private List<Map<String, String>> syncz(String istioUrl) {
        try {
            String body = getForEntity(istioUrl + "/debug/syncz", String.class).getBody();
            if (!StringUtils.isEmpty(body)) {
                List<Map<String, String>> result = objectMapper.readValue(body, new TypeReference<List<Map<String, String>>>() {});
                if (result != null) {
                    return result;
                } else {
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            logger.error("error while getting pod sync data", e);
        }
        return new ArrayList<>();
    }

    public String invokeIstiod(String podName, String podNamespace, String path, Map<String, String> params) {
        Pod pod = client.getObject(K8sResourceEnum.Pod.name(), podNamespace, podName);
        String istioUrl = String.format("http://%s:8080", pod.getStatus().getPodIP());
        return getForEntity(istioUrl + path, String.class).getBody();
    }

    private List<String> getIstioPodUrls() {
        return pilotNamespaces.stream()
            .flatMap(ns -> getUrls(ns).stream())
            .collect(Collectors.toList());
    }

    private List<String> getUrls(String ns) {
        String podUrl = client.getUrlWithLabels(K8sResourceEnum.Pod.name(), ns, ImmutableMap.of("app", NAME)) + "&fieldSelector=status.phase%3DRunning";
        return client.<Pod>getObjectList(podUrl).stream()
            .map(p -> String.format("http://%s:8080", p.getStatus().getPodIP()))
            .collect(Collectors.toList());
    }

    private String getIstioUrl() {
        if (!StringUtils.isEmpty(istioHttpUrl)) return istioHttpUrl;
        List<Service> pilotServices = client.getObjectList(K8sResourceEnum.Service.name(), NAMESPACE, ImmutableMap.of("app", NAME));
        if (CollectionUtils.isEmpty(pilotServices)) throw new ApiPlaneException(ExceptionConst.PILOT_SERVICE_NON_EXIST);
        Service service = pilotServices.get(0);
        String ip = service.getSpec().getClusterIP();
        List<ServicePort> ports = service.getSpec().getPorts();
        //get port by name equal  http-legacy-discovery
        for (ServicePort port : ports) {
            if ("http-legacy-discovery".equalsIgnoreCase(port.getName())) {
                return String.format("http://%s:%s", ip, port.getPort());
            }
        }
        //if http-legacy-discovery not found
        //default port
        String port = "8080";
        return String.format("http://%s:%s", ip, port);
    }

    public Map<String, String> getSidecarSyncStatus(String name, String namespace) {
        try {
            return statusCache.get(PILOT_GLOBAL_KEY).get(String.format("%s.%s", name, namespace));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Map<String, String>> getSidecarSyncStatusFromPilot(String type, String version) {
        try {
            return statusCache.get(String.format("%s/%s", type, version));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getEndpoints() {
        try {
            return (String) endpointsCache.get("endpoints");
        } catch (ExecutionException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    private List<Endpoint> getEndpointList() {
        List<Endpoint> endpoints = new ArrayList<>();
        //fmt.Fprintf(w, "%s:%s %s:%d %v %s\n", ss.Hostname,
        //            p.Name, svc.Endpoint.Address, svc.Endpoint.EndpointPort, svc.Endpoint.Labels,
        //            svc.Endpoint.ServiceAccount)

        String[] rawValues = StringUtils.split(getEndpoints(), "\n");
        for (String rawValue : rawValues) {
            String[] segments = StringUtils.splitPreserveAllTokens(rawValue, " ");
            if (ArrayUtils.getLength(segments) != 4) {
                continue;
            }
            String[] hostNameProtocol = StringUtils.splitPreserveAllTokens(segments[0], ":");
            String[] ipPort = StringUtils.splitPreserveAllTokens(segments[1], ":");
            Map<String, String> labelMap = new HashMap<>();
            String[] labels = StringUtils.split(segments[2], ",");
            for (String label : labels) {
                String[] kv = StringUtils.splitPreserveAllTokens(label, "=");
                if (ArrayUtils.getLength(kv) == 2) {
                    labelMap.put(kv[0], kv[1]);
                }
            }
            Endpoint ep = new Endpoint();
            ep.setHostname(hostNameProtocol[0]);
            ep.setProtocol(hostNameProtocol[1]);
            ep.setAddress(ipPort[0]);
            ep.setPort(Integer.valueOf(ipPort[1]));
            ep.setLabels(labelMap);
            endpoints.add(ep);
        }
        return endpoints;

//        for (String rawValue : rawValues) {
//            String[] segments = StringUtils.splitPreserveAllTokens(rawValue, " ");
//            //相对于/debug/endpointz?brief=true
//            // /debug/endpointz?brief=true&instancePort=true接口增加了Endpoint端口
//            if (ArrayUtils.getLength(segments) < 5) {
//                continue;
//            }
//            String[] hostNameProtocol = StringUtils.splitPreserveAllTokens(segments[0], ":");
//            String[] ipPort = StringUtils.splitPreserveAllTokens(segments[2], ":");
//            Map<String, String> labelMap = new HashMap<>();
//            String[] labels = StringUtils.split(segments[3], ",");
//            for (String label : labels) {
//                String[] kv = StringUtils.splitPreserveAllTokens(label, "=");
//                if (ArrayUtils.getLength(kv) == 2) {
//                    labelMap.put(kv[0], kv[1]);
//                }
//            }
//            Endpoint ep = new Endpoint();
//            ep.setHostname(hostNameProtocol[0]);
//            ep.setProtocol(hostNameProtocol[1]);
//            ep.setAddress(ipPort[0]);
//            //service entry port
//            ep.setPort(Integer.valueOf(ipPort[1]));
//            ep.setLabels(labelMap);
//            fixDubboEndPoint(hostNameProtocol, ep, segments[segments.length - 1]);
//            endpoints.add(ep);
//        }
//        return endpoints;
    }

    /**
     * 解析 dubbo 信息
     * dubbo hostName 以 : 拼接, {@link this#getEndpointList()} 方法中的hostNameProtocol提取将会产生问题， 在本方法中处理
     * @param hostNameProtocol
     * @param ep
     * @return
     */
    private void fixDubboEndPoint(String[] hostNameProtocol, Endpoint ep, String endpointPort) {
        if (ObjectUtils.isEmpty(hostNameProtocol)) {
            return;
        }
        if (!Const.PROTOCOL_DUBBO.equalsIgnoreCase(hostNameProtocol[hostNameProtocol.length - 1])) {
            return;
        }
        hostNameProtocol[hostNameProtocol.length - 1] = null;
        ep.setHostname(CommonUtil.removeEnd(":", StringUtils.joinWith(":", hostNameProtocol)));
        Map<String, String> labels = ep.getLabels();
        if (CollectionUtils.isEmpty(labels)) {
            labels = new HashMap<>();
        }
        labels.put(Const.DUBBO_TCP_PORT, endpointPort);
        ep.setProtocol(Const.PROTOCOL_DUBBO);
    }

    public List<String> getServiceList(Predicate<Endpoint> filter) {
        if (Objects.isNull(filter)) {
            filter = e -> true;
        }
        return getEndpointList().stream().filter(filter).map(Endpoint::getHostname).distinct().collect(Collectors.toList());
    }

    public List<Endpoint> getEndpointList(Predicate<Endpoint> filter) {
        if (Objects.isNull(filter)) {
            filter = e -> true;
        }
        return getEndpointList().stream().filter(filter).distinct().collect(Collectors.toList());
    }

    public List<Gateway> getGatewayList(Predicate<Gateway> filter) {
        if (Objects.isNull(filter)) {
            filter = e -> true;
        }
        List<Gateway> gateways = getEndpointList().stream().map(endpoint -> {
            Gateway gateway = new Gateway();
            gateway.setAddress(endpoint.getAddress());
            gateway.setHostname(endpoint.getHostname());
            gateway.setLabels(endpoint.getLabels());
            return gateway;
        }).filter(filter).distinct().collect(Collectors.toList());
        Map<String, Gateway> temp = new HashMap<>();
        gateways.forEach(gateway -> temp.putIfAbsent(gateway.getAddress(), gateway));
        return new ArrayList<>(temp.values());
    }


  /*  public List<IstioGateway> getIstioGateway(Predicate<Gateway> filter) {
        if (Objects.isNull(filter)) {
            filter = e -> true;
        }
        List<IstioGateway> gateways = getEndpointList().stream().map(endpoint -> {
            IstioGateway gateway = new IstioGateway();
            gateway.setName(endpoint.get());
            gateway.setHostname(endpoint.getHostname());
            gateway.setLabels(endpoint.getLabels());
            return gateway;
        }).filter(filter).distinct().collect(Collectors.toList());
        Map<String, IstioGateway> temp = new HashMap<>();
        gateways.forEach(gateway -> temp.putIfAbsent(gateway.getGwCluster(), gateway));
        return new ArrayList<>(temp.values());
    }*/

    public boolean isReady() {
        ResponseEntity<String> resp = shortTimeoutRestTemplate.exchange(getIstioUrl() + HEALTH_CHECK_PATH,
                HttpMethod.GET, null, String.class);
        if (resp.getStatusCode() != HttpStatus.OK) return false;
        return true;
    }

    private <T> ResponseEntity<T> getForEntity(String str, Class<T> clz) {

        ResponseEntity<T> entity;
        try {
            entity = restTemplate.getForEntity(str, clz);
        } catch (Exception e) {
            logger.warn("", e);
            throw new ApiPlaneException(e.getMessage());
        }
        return entity;
    }
}


