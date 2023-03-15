package org.hango.cloud.core.gateway.service.impl;

import org.hango.cloud.core.envoy.EnvoyHttpClient;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.istio.PilotHttpClient;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.HealthServiceSubset;
import org.hango.cloud.meta.ServiceAndPort;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.util.CommonUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.util.Const.*;

@Component
public class DefaultResourceManager implements ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceManager.class);

    @Autowired
    private PilotHttpClient istioHttpClient;

    @Autowired
    private EnvoyHttpClient envoyHttpClient;

    @Value("${service.namespace.exclude:gateway-system,kube-system,istio-system,gateway-v112}")
    private String excludeNamespace;

    private List<String> getExcludeNamespace() {
        List<String> ret = new ArrayList<>();
        if (StringUtils.isEmpty(excludeNamespace)) return ret;
        ret.addAll(Arrays.asList(excludeNamespace.split(",")));
        return ret;
    }

    @Override
    public List<Endpoint> getEndpointList() {
        return istioHttpClient.getEndpointList().stream()
                //port为空过滤
                .filter(e -> e.getPort() != null)
                //ns过滤
                .filter(e -> namespaceFilter(e.getHostname()))
                .collect(Collectors.toList());
    }


    @Override
    public List<String> getServiceList() {
        return istioHttpClient.getEndpointList().stream()
                .map(Endpoint::getHostname)
                //过滤静态服务
                .filter(this::notStaticService)
                //过滤网关ns
                .filter(this::namespaceFilter)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceAndPort> getServiceAndPortList(Map<String, String> filters) {
        Map<String, Map<String, String>> kindFilters = generateKindFilters(filters);
        Map<String, List<Endpoint>> endpointmap = getEndpointList().stream()
                .filter(e -> notStaticService(e.getHostname()))
                .filter(e -> isMatchFilter(e, kindFilters))
                .collect(Collectors.groupingBy(Endpoint::getHostname));
        logger.info("[get service] after service filtering, servicePortMap length: {}", endpointmap.size());

        return endpointmap.entrySet().stream()
                .map(entry -> {
                    ServiceAndPort sap = new ServiceAndPort();
                    List<Integer> ports = entry.getValue().stream().map(Endpoint::getPort).distinct().collect(Collectors.toList());
                    sap.setName(entry.getKey());
                    sap.setPort(ports);
                    return sap;
                }).collect(Collectors.toList());
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

    private boolean notStaticService(String hostname) {
        return !StringUtils.contains(hostname, "com.netease.static");
    }

    /**
     * 过滤网关自身服务
     */
    private boolean namespaceFilter(String hostName){
        String[] segments = StringUtils.split(hostName, ".");
        if (ArrayUtils.getLength(segments) != 5) {
            return true;
        }
        return !getExcludeNamespace().contains(segments[1]);
    }

    /**
     * 判断endpoint是否满足所有类型过滤器条件
     *
     * @param endpoint    服务实例数据结构
     * @param kindFilters 所有类型过滤器集合
     * @return 是否满足所有过滤器条件
     */
    private boolean isMatchFilter(Endpoint endpoint, Map<String, Map<String, String>> kindFilters) {
        if (!isMatchFilter(endpoint, kindFilters, PREFIX_LABEL)) {
            return false;
        }
        if (!isMatchFilter(endpoint, kindFilters, PROJECT_LABEL)) {
            return false;
        }
        if (!isMatchFilter(endpoint, kindFilters, PREFIX_HOST)) {
            return false;
        }
        if (!isMatchFilter(endpoint, kindFilters, PREFIX_PROTOCOL)) {
            return false;
        }
        return true;
    }

    /**
     * 判断endpoint是否满足某类型过滤器条件
     *
     * @param endpoint     服务实例数据结构
     * @param kindFilters  所有类型过滤器集合
     * @param filterPrefix 过滤器类型
     * @return 是否满足所有过滤器条件
     */
    private boolean isMatchFilter(Endpoint endpoint, Map<String, Map<String, String>> kindFilters, String filterPrefix) {
        if (!CollectionUtils.isEmpty(kindFilters.get(filterPrefix))) {
            Map<String, String> filters = kindFilters.get(filterPrefix);

            if (PROJECT_LABEL.equals(filterPrefix)) {
                // 项目隔离过滤器，用于对开启项目隔离的endpoint进行基于projectCode过滤
                if (!passProjectCodeMatch(endpoint, kindFilters)) {
                    return false;
                }
            }

            if (PREFIX_LABEL.equals(filterPrefix)) {
                // label匹配逻辑
                for (Map.Entry<String, String> entry : filters.entrySet()) {
                    if (!entry.getValue().equals(endpoint.getLabels().get(entry.getKey()))) {
                        return false;
                    }
                }
            }

            // 除了projectCode和label外的其他类型过滤标签
            if (!PROJECT_LABEL.equals(filterPrefix) && !PREFIX_LABEL.equals(filterPrefix)) {
                for (Map.Entry<String, String> entry : filters.entrySet()) {
                    if (!entry.getValue().equals(getEndpointAttrByPrefix(filterPrefix, endpoint))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean passProjectCodeMatch(Endpoint endpoint, Map<String, Map<String, String>> kindFilters) {
        Map<String, String> labelFilters = kindFilters.get(PROJECT_LABEL);
        String hostname = endpoint.getHostname();
        if (StringUtils.isEmpty(hostname)) {
            logger.warn("[isMatchFilter] exist empty hostname, endpoint: {}", endpoint);
            return false;
        }
        if (hostname.matches(SERVICE_PROJECT_ISOLATION_FORMAT)) {
            // 开启项目隔离功能的服务endpoint校验是否匹配projectCode
            String hostRegex = "^.+\\.nsf\\." + labelFilters.get(PROJECT_CODE) + "\\.(eureka|nacos)$";
            return hostname.matches(hostRegex);
        }
        return true;
    }

    /**
     * 通过前缀获取Endpoint对象指定字段
     *
     * @param prefix   匹配前缀
     * @param endpoint 服务实例数据结构
     * @return Endpoint的指定字段
     */
    private String getEndpointAttrByPrefix(String prefix, Endpoint endpoint) {
        String result = "";
        switch (prefix) {
            case PREFIX_ADDRESS:
                result = endpoint.getAddress();
                break;
            case PREFIX_HOST:
                result = endpoint.getHostname();
                break;
            case PREFIX_PORT:
                result = String.valueOf(endpoint.getPort());
                break;
            case PREFIX_PROTOCOL:
                result = endpoint.getProtocol();
                break;
            default:
        }
        return result;
    }

    /**
     * 根据gportal传来的原生过滤条件创建各类过滤器，进行如下转换
     * filters:
     * {
     *   "label_projectCode": "project1",
     *   "label_application": "app1",
     *   "action": "function",
     *   "host_": "qz.com"
     * }
     * 转化为如下过滤器
     * kindFilters:
     * {
     *   "label_": {
     *     "projectCode": "project1",
     *     "application": "app1"
     *   },
     *   "host_": {
     *     "host_0": "qz.com"
     *   }
     * }
     *
     * @param filters 原生条件过滤器（其中包含无效信息，需要处理）
     * @return kindFilters(根据endpoint结构分类的条件过滤器)
     */
    public Map<String, Map<String, String>> generateKindFilters(Map<String, String> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Collections.EMPTY_MAP;
        }
        Map<String, Map<String, String>> kindFilters = new HashMap<>(8);
        Set<String> keys = filters.keySet();
        for (String key : keys) {
            if (key.equals(PROJECT_LABEL)) {
                fillKindFilters(kindFilters, PROJECT_LABEL, key, filters.get(key));
            } else if (key.startsWith(PREFIX_LABEL)) {
                fillKindFilters(kindFilters, PREFIX_LABEL, key, filters.get(key));
            } else if (key.startsWith(PREFIX_HOST)) {
                fillKindFilters(kindFilters, PREFIX_HOST, key, filters.get(key));
            } else if (key.startsWith(PREFIX_PROTOCOL)) {
                fillKindFilters(kindFilters, PREFIX_PROTOCOL, key, filters.get(key));
            }
        }
        return kindFilters;
    }

    /**
     * 根据过滤条件前缀，填充新的过滤器
     *
     * @param kindFilters  包含各种过滤器的集合；包含"标签匹配过滤器"、"域名匹配过滤器"等等，全部类型参考"generateKindFilters"方法
     * @param filterPrefix 匹配条件前缀，用于判定是何种类型的匹配器；例如"label_:标签匹配；host_:域名匹配"
     * @param key          原生的匹配条件，若符合匹配条件，需要去除前缀后使用；例如"label_application"
     * @param value        匹配条件对应的匹配值
     */
    private void fillKindFilters(Map<String, Map<String, String>> kindFilters, String filterPrefix, String key, String value) {
        // 创建对应匹配规则的过滤器
        if (!kindFilters.containsKey(filterPrefix)) {
            Map<String, String> filters = new HashMap<>(2);
            kindFilters.put(filterPrefix, filters);
        }
        if (PREFIX_LABEL.equals(filterPrefix)) {
            String filterKey = key.substring(PREFIX_LABEL.length());
            // label匹配规则直接以kv形式填充
            kindFilters.get(filterPrefix).put(filterKey, value);
        } else if (PROJECT_LABEL.equals(filterPrefix)) {
            // 项目隔离过滤器
            String filterKey = key.substring(PREFIX_LABEL.length());
            kindFilters.get(PROJECT_LABEL).put(filterKey, value);
        } else {
            // 其他匹配规则将key以序号形式填充
            Map<String, String> filters = kindFilters.get(filterPrefix);
            filters.put(filterPrefix + filters.size(), value);
        }
    }
}
