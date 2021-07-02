package org.hango.cloud.util;

public interface Const {

    String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";

    //http 方法
    String GET_METHOD = "GET";

    String POST_METHOD = "POST";

    String PUT_METHOD = "PUT";

    String HEAD_METHOD = "HEAD";

    String DELETE_METHOD = "DELETE";

    String OPTIONS_METHOD = "OPTIONS";

    // 默认请求头
    String INTERFACE_CALL_TYPE_INNER = "inner";
    String ACCEPT_LANGUAGE_ZH = "zh";

    String OPTION_TRUE = "true";
    String OPTION_FALSE = "false";
    String OPTION_ENABLED = "enabled";
    String OPTION_DISABLED = "disabled";


    String SERVICE_TYPE_CONSUL = "Consul";
    String SERVICE_TYPE_K8S = "Kubernetes";

    // DestinationRule loadbalancer type

    String LB_TYPE_ROUND_ROBIN = "ROUND_ROBIN";
    String LB_TYPE_RANDOM = "RANDOM";
    String LB_TYPE_LEAST_CONN = "LEAST_CONN";
    String LB_TYPE_PASSTHROUGH = "PASSTHROUGH";
    String LB_TYPE_CONSISTENT_HASH = "CONSISTENT_HASH";
}
