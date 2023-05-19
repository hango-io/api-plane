package org.hango.cloud.util;

import java.util.Arrays;
import java.util.List;

public interface Const {

    String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";

    String LABEL_NSF_ENV = "nsf-env";

    String GATEWAY = "Gateway";


    //http 方法
    String GET_METHOD = "GET";

    String POST_METHOD = "POST";

    // 默认请求头
    String INTERFACE_CALL_TYPE_INNER = "inner";
    String ACCEPT_LANGUAGE_ZH = "zh";

    String OPTION_TRUE = "true";
    String SERVICE_TYPE_CONSUL = "Consul";
    String SERVICE_TYPE_K8S = "Kubernetes";
    String SERVICE_TYPE_DUBBO = "Zookeeper";
    String SERVICE_TYPE_EUREKA = "Eureka";

    String SERVICE_TYPE_NACOS = "Nacos";
    List<String> VAILD_REGISTRY = Arrays.asList(SERVICE_TYPE_CONSUL, SERVICE_TYPE_K8S, SERVICE_TYPE_DUBBO, SERVICE_TYPE_EUREKA,SERVICE_TYPE_NACOS);

    String PROTOCOL_DUBBO = "dubbo";
    String DUBBO_APPLICATION = "application";
    String DUBBO_TCP_PORT = "skiff_dubbo_tcp_port";
    String DUBBO_SERVICE_SUFFIX = ".dubbo";
    /**
     * eureka服务endpoint的hosts后缀
     */
    String EUREKA_SERVICE_SUFFIX = ".eureka";

    String SIDECAR_CONTAINER = "istio-proxy";

    String WORKLOAD_UPDATE_TIME_ANNOTATION = "nsf_workload_update_time";
    String WORKLOAD_OPERATION_TYPE_ANNOTATION = "nsf_workload_operation_type";
    String CONTAINER_STATUS_RUNNING = "Running";
    String CONTAINER_STATUS_WAITING = "Waiting";
    String CONTAINER_STATUS_TERMINATED = "Terminated";

    /**
     * 查询服务接口的过滤条件前缀字符
     */
    public static final String PREFIX_LABEL = "label_";
    public static final String PREFIX_HOST = "host_";
    public static final String PREFIX_ADDRESS = "address_";
    public static final String PREFIX_PORT = "port_";
    public static final String PREFIX_PROTOCOL = "protocol_";
    String PROJECT_LABEL = "label_projectCode";

    String PROJECT_CODE = "projectCode";

    /**
     * envoy outbound prefix
     */
    String OUTBOUND = "outbound";

    /**
     * 认证类型
     */
    String AUTH_JWKS = "Jwks";

    /**
     * 以下是envoy filter全名
     */
    String JWT_FILTER = "envoy.filters.http.jwt_authn";

    /**
     * RBAC插件身份信息存储 filter
     */
    String RBAC_IDENTITY_FILTER = "envoy.filters.http.rbac_identity";

    /**
     * 项目隔离服务host正则表达式
     */
    String SERVICE_PROJECT_ISOLATION_FORMAT = "^.+\\.nsf\\..+\\.(eureka|nacos)$";
}
