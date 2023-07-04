package org.hango.cloud.util;

import java.util.Arrays;
import java.util.List;

public class Const {

    public static final String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    public static final String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";

    public static final String GATEWAY = "Gateway";


    //http 方法
    public static final String GET_METHOD = "GET";

    public static final String POST_METHOD = "POST";

    // 默认请求头
    public static final String INTERFACE_CALL_TYPE_INNER = "inner";
    public static final String ACCEPT_LANGUAGE_ZH = "zh";

    public static final String OPTION_TRUE = "true";
    public static final String SERVICE_TYPE_CONSUL = "Consul";
    public static final String SERVICE_TYPE_K8S = "Kubernetes";
    public static final String SERVICE_TYPE_DUBBO = "Zookeeper";
    public static final String SERVICE_TYPE_EUREKA = "Eureka";

    public static final String SERVICE_TYPE_NACOS = "Nacos";
    public static final List<String> VAILD_REGISTRY = Arrays.asList(SERVICE_TYPE_CONSUL, SERVICE_TYPE_K8S, SERVICE_TYPE_DUBBO, SERVICE_TYPE_EUREKA,SERVICE_TYPE_NACOS);

    public static final String PROTOCOL_DUBBO = "dubbo";
    public static final String DUBBO_APPLICATION = "application";
    public static final String DUBBO_TCP_PORT = "skiff_dubbo_tcp_port";
    public static final String DUBBO_SERVICE_SUFFIX = ".dubbo";

    public static final String PROTOCOL_HTTP = "HTTP";
    public static final String PROTOCOL_HTTPS = "HTTPS";
    public static final String PROTOCOL_TCP = "TCP";
    public static final String PROTOCOL_UDP = "UDP";

    /**
     * 查询服务接口的过滤条件前缀字符
     */
    public static final String PREFIX_LABEL = "label_";
    public static final String PREFIX_HOST = "host_";
    public static final String PREFIX_ADDRESS = "address_";
    public static final String PREFIX_PORT = "port_";
    public static final String PREFIX_PROTOCOL = "protocol_";
    public static final String PROJECT_LABEL = "label_projectCode";

    public static final String PROJECT_CODE = "projectCode";


    /**
     * 认证类型
     */
    public static final String AUTH_JWKS = "Jwks";

    /**
     * 以下是envoy filter全名
     */
    public static final String JWT_FILTER = "envoy.filters.http.jwt_authn";

    /**
     * RBAC插件身份信息存储 filter
     */
    public static final String RBAC_IDENTITY_FILTER = "envoy.filters.http.rbac_identity";

    /**
     * 项目隔离服务host正则表达式
     */
    public static final String SERVICE_PROJECT_ISOLATION_FORMAT = "^.+\\.nsf\\..+\\.(eureka|nacos)$";

    /**
     * 插件配置路径前缀
     */
    public static final String PLUGIN_PATH_PREFIX = "plugin/route/";

    public static final String PLUGIN_MANAGER_TEMPLATE = "pluginmanager-template.json";

    /**
     * 插件处理器
     */
    public static final String PLUGIN_PROCESSOR = "AggregateGatewayPluginProcessor";

    /**
     * 自定义插件默认作者
     */
    public static final String SYSTEM = "system";

    /**
     * 文件
     */
    public static final String FILE = "file";


    public static final String LUA = "lua";

    public static final String RIDER_PLUGIN = "proxy.filters.http.rider";


}
