package org.hango.cloud.core.istio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import net.minidev.json.JSONObject;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.dto.PortalServiceDTO;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.util.exception.ExceptionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
public class PilotHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(PilotHttpClient.class);

    @Value(value = "${istioNamespace:gateway-system}")
    private String NAMESPACE;

    @Value("#{ '${istioNamespaces:istio-system}'.split(',') }")
    private List<String> pilotNamespaces;

    @Value(value = "${istioName:istiod}")
    private String ISTIO_NAME;

    @Value(value = "${meshRegistryName:galley}")
    private String MESH_REGISTRY_NAME;

    private static final String GET_ENDPOINTZ_PATH = "/debug/endpointz?brief=true";

    private static final String GET_ZK_PATH = "/zk?interfaceName=";

    @Value(value = "${istioHttpUrl:#{null}}")
    private String istioHttpUrl;

    @Value(value = "${meshRegistryHttpUrl:#{null}}")
    private String meshRegistryHttpUrl;

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

    public static final Integer ERROR_PORT = -1;

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

    private String getIstioUrl() {
        if (!StringUtils.isEmpty(istioHttpUrl)) return istioHttpUrl;
        return getSvcUrl(ISTIO_NAME);
    }

    private String getMeshRegistryUrl() {
        if (!StringUtils.isEmpty(meshRegistryHttpUrl)) return meshRegistryHttpUrl;
        return getSvcUrl(MESH_REGISTRY_NAME);
    }

    public List<Endpoint> getDubboEndpoints(String igv){
        String interfaceName = igv.split(":")[0];
        String body = getForEntity(getMeshRegistryUrl() + GET_ZK_PATH + interfaceName, String.class).getBody();
        if (StringUtils.isBlank(body)){
            return new ArrayList<>();
        }
        try {
            return parseDubboInfo(body, igv);
        } catch (Exception e) {
            logger.error("解析dubbo信息失败，body：{}， igv:{}", body, igv);
        }
        return new ArrayList<>();
    }

    public List<Endpoint> parseDubboInfo(String str, String igv){
        List<Endpoint> endpointList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode cache;
        try {
            cache = objectMapper.readTree(str).get("cache");
        } catch (JsonProcessingException e) {
            logger.error("parse dubbo info error, str:{}", str, e);
            return endpointList;
        }
        JsonNode cluster = cache.get(igv);
        if (cluster == null){
            return endpointList;
        }
        JsonNode serviceEntry = cluster.get("ServiceEntry");
        if (serviceEntry == null){
            return endpointList;
        }
        JsonNode ports = serviceEntry.get("ports");
        if (ports == null){
            return endpointList;
        }
        JsonNode port = ports.get(0);
        if (port == null){
            return endpointList;
        }
        JsonNode protocalNode = port.get("protocal");
        JsonNode numberNode = port.get("number");
        String protocal = protocalNode == null ? Const.PROTOCOL_DUBBO : protocalNode.asText();
        Integer number = numberNode == null ? 80 : numberNode.asInt();
        JsonNode endpoints = serviceEntry.get("endpoints");
        if (endpoints == null){
            return endpointList;
        }
        for (JsonNode endpoint : endpoints) {
            Endpoint ep = new Endpoint();
            ep.setHostname(igv);
            ep.setProtocol(protocal);
            ep.setPort(number);
            JsonNode address = endpoint.get("address");
            if (address != null){
                ep.setAddress(address.asText());
            }
            JsonNode labels = endpoint.get("labels");
            if (labels != null){
                Map<String, String> labelMap = objectMapper.convertValue(labels, new TypeReference<Map<String, String>>(){});
                ep.setLabels(labelMap);
            }
            JsonNode dubboPorts = endpoint.get("ports");
            if (dubboPorts != null){
                Map<String, Integer> portRes = objectMapper.convertValue(dubboPorts, new TypeReference<Map<String, Integer>>(){});
                Integer dubboPort = portRes.values().stream().findFirst().orElse(null);
                if (dubboPort != null && dubboPort >= 0){
                    Map<String, String> dubboLabel = CollectionUtils.isEmpty(ep.getLabels())? new HashMap<>() : ep.getLabels();
                    dubboLabel.put(Const.DUBBO_TCP_PORT, String.valueOf(dubboPort));
                    ep.setLabels(dubboLabel);
                }
            }
            endpointList.add(ep);
        }

        return endpointList;
    }

    private String getEndpoints() {
        try {
            return (String) endpointsCache.get("endpoints");
        } catch (ExecutionException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public List<Endpoint> getEndpointList() {
        List<Endpoint> endpoints = new ArrayList<>();
        String[] rawValues = StringUtils.split(getEndpoints(), "\n");
        for (String rawValue : rawValues) {
            String[] segments = StringUtils.splitPreserveAllTokens(rawValue, " ");
            if (ArrayUtils.getLength(segments) != 4) {
                continue;
            }
            /**
             * segments[0]:com.netease.cloud.nsf.demo.stock.api.EchoService:A:dubbo
             * segments[1]:10.178.249.42:80
             * segments[2]:application=spring-cloud-dubbo,deprecated=false,dubbo=2.0.2,group=group-a,interface=com.netease.apigateway.dubbo.api.GatewayEchoService
             */

            Endpoint ep = new Endpoint();
            //解析hostname和protocal
            parseHostInfo(ep, segments[0]);
            //解析ip+port
            String[] ipPort = StringUtils.splitPreserveAllTokens(segments[1], ":");
            //解析label
            Map<String, String> labelMap = new HashMap<>();
            String[] labels = StringUtils.split(segments[2], ",");
            for (String label : labels) {
                String[] kv = StringUtils.splitPreserveAllTokens(label, "=");
                if (ArrayUtils.getLength(kv) == 2) {
                    labelMap.put(kv[0], kv[1]);
                }
            }
            ep.setAddress(ipPort[0]);
            ep.setPort(Integer.valueOf(ipPort[1]));
            ep.setLabels(labelMap);
            endpoints.add(ep);
        }
        return endpoints.stream().filter(o -> StringUtils.isNotBlank(o.getHostname())).collect(Collectors.toList());
    }

    /**
     * 解析hostname和protocal信息
     * hostNameProtocal: com.netease.cloud.nsf.demo.stock.api.EchoService:A:dubbo
     * hostname: com.netease.cloud.nsf.demo.stock.api.EchoService:A
     * protocal: dubbo
     */
    private void parseHostInfo(Endpoint ep, String hostNameProtocal){
        if (StringUtils.isBlank(hostNameProtocal)){
            return;
        }
        //获取最后一个冒号的位置
        int index = hostNameProtocal.lastIndexOf(":");
        //基于最后一个冒号分割，前半段为hostName, 后半段为协议
        String hostName = hostNameProtocal.substring(0, index);
        String protocal = hostNameProtocal.substring(index+1);
        ep.setHostname(hostName);
        ep.setProtocol(protocal);
    }

    public <T> ResponseEntity<T> getForEntity(String str, Class<T> clz) {

        ResponseEntity<T> entity;
        try {
            entity = restTemplate.getForEntity(str, clz);
        } catch (Exception e) {
            logger.warn("", e);
            throw new ApiPlaneException(e.getMessage());
        }
        return entity;
    }

    public String getSvcUrl(String svcName) {
        List<Service> pilotServices = client.getObjectList(K8sResourceEnum.Service.name(), NAMESPACE, ImmutableMap.of("app", svcName));
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

}


