package org.hango.cloud.util;

public interface Const {

    String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";

    String LABEL_NSF_ENV = "nsf-env";


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
    String PROTOCOL_DUBBO = "dubbo";
    String DUBBO_APPLICATION = "application";
    String DUBBO_TCP_PORT = "skiff_dubbo_tcp_port";
    String DUBBO_SERVICE_SUFFIX = ".dubbo";

    String SIDECAR_CONTAINER = "istio-proxy";

    String WORKLOAD_UPDATE_TIME_ANNOTATION = "nsf_workload_update_time";
    String WORKLOAD_OPERATION_TYPE_ANNOTATION = "nsf_workload_operation_type";
    String CONTAINER_STATUS_RUNNING = "Running";
    String CONTAINER_STATUS_WAITING = "Waiting";
    String CONTAINER_STATUS_TERMINATED = "Terminated";

    /**
     * envoy outbound prefix
     */
    String OUTBOUND = "outbound";
}
