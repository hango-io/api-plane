package org.hango.cloud.core.template;

/**
 * 支持TemplateWrapper的regex expression
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface TemplateConst {
    String DESCRIPTION_TAG = "(?m)^#(?!@)(.*)$";
    String LABEL_TAG = "(?m)^#@(.*)=(.*)$";
    String BLANK_LINE = "(?m)^\\s*$(?:\\n|\\r\\n)";
    String IGNORE_SCHEME = "(?m)^(?!#)(.*)$";


    String LABEL_RESOURCE_IDENTITY = "skiff-api-plane-resource-identity";
    String LABEL_API_PLANE_TYPE = "skiff-api-plane-type";
    String LABEL_API_PLANE_VERSION = "skiff-api-plane-version";
    String LABLE_ISTIO_REV = "istio.io/rev";
    /** ---------- 模板占位符名 begin ---------- **/

    /**
     * 公用
     **/

    String NAMESPACE = "t_namespace";
    String API_GATEWAY = "t_api_gateway";
    String API = "t_api";
    String API_SERVICE = "t_api_service";
    String API_NAME = "t_api_name";
    String RESOURCE_IDENTITY = "t_resource_identity";
    /**
     * API唯一标识，用于api name重复时
     */
    String API_IDENTITY_NAME = "t_api_identity_name";
    String API_LOADBALANCER = "t_api_loadBalancer";
    String API_CONNECT_TIMEOUT = "t_api_connect_timeout";
    String API_IDLE_TIMEOUT = "t_api_idle_timeout";
    String API_RETRIES = "t_api_retries";
    String API_PRESERVE_HOST = "t_api_preserve_host";
    String API_PRIORITY = "t_api_priority";


    /**
     * VirtualService
     **/

    String VIRTUAL_SERVICE_NAME = "t_virtual_service_name";
    String VIRTUAL_SERVICE_SUBSET_NAME = "t_virtual_service_subset_name";
    String VIRTUAL_SERVICE_DESTINATIONS = "t_virtual_service_destinations";
    String VIRTUAL_SERVICE_MATCH_YAML = "t_virtual_service_match_yaml";
    String VIRTUAL_SERVICE_ROUTE_YAML = "t_virtual_service_route_yaml";
    String VIRTUAL_SERVICE_EXTRA_YAML = "t_virtual_service_extra_yaml";
    String VIRTUAL_SERVICE_META_YAML = "t_virtual_service_meta_yaml";
    String VIRTUAL_SERVICE_MATCH_PRIORITY_YAML = "t_virtual_service_priority_yaml";
    String VIRTUAL_SERVICE_HTTP_RETRY_YAML = "t_virtual_service_http_retry_yaml";
    String VIRTUAL_SERVICE_HOSTS = "t_virtual_service_hosts";
    String VIRTUAL_SERVICE_HOST_HEADERS = "t_virtual_service_host_headers";
    String VIRTUAL_SERVICE_MATCH_PRIORITY = "t_virtual_service_match_priority";
    String VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY = "t_virtual_service_plugin_match_priority";
    String VIRTUAL_SERVICE_URL_MATCH = "t_virtual_service_url_match";
    String VIRTUAL_SERVICE_SERVICE_TAG = "t_virtual_service_service_tag";
    String VIRTUAL_SERVICE_API_ID = "t_virtual_service_api_id";
    String VIRTUAL_SERVICE_TENANT_ID = "t_virtual_service_tenant_id";
    String VIRTUAL_SERVICE_PROJECT_ID = "t_virtual_service_project_id";
    String VIRTUAL_SERVICE_API_NAME = "t_virtual_service_api_name";
    String VIRTUAL_SERVICE_TIME_OUT = "t_virtual_service_timeout";
    String VIRTUAL_SERVICE_RETRY_ATTEMPTS = "t_http_retry_attempts";
    String VIRTUAL_SERVICE_RETRY_PER_TIMEOUT = "t_http_retry_perTryTimeout";
    String VIRTUAL_SERVICE_RETRY_RETRY_ON = "t_http_retry_retryOn";
    String VIRTUAL_SERVICE_REQUEST_HEADERS = "t_virtual_service_request_headers";
    String VIRTUAL_SERVICE_VIRTUAL_CLUSTER_NAME = "t_virtual_service_virtual_cluster_name";
    String VIRTUAL_SERVICE_VIRTUAL_CLUSTER_HEADERS = "t_virtual_service_virtual_cluster_headers";
    String VIRTUAL_SERVICE_MIRROR_SERVICE = "t_virtual_service_mirror_service";
    String VIRTUAL_SERVICE_MIRROR_PORT = "t_virtual_service_mirror_port";
    String VIRTUAL_SERVICE_MIRROR_SUBSET = "t_virtual_service_mirror_subset";
    String VIRTUAL_SERVICE_MIRROR_YAML = "t_virtual_service_mirror_yaml";
    String VIRTUAL_SERVICE_STATS = "t_virtual_service_stats";

    /**
     * ServiceInfo
     **/
    String SERVICE_INFO_API_GATEWAY = "t_service_info_api_gateway";
    String SERVICE_INFO_API_NAME = "t_service_info_api_name";
    String SERVICE_INFO_API_SERVICE = "t_service_info_api_service";
    String SERVICE_INFO_API_METHODS = "t_service_info_api_methods";
    String SERVICE_INFO_API_REQUEST_URIS = "t_service_info_api_request_uris";
    String SERVICE_INFO_VIRTUAL_SERVICE_SUBSET_NAME = "t_service_info_virtual_service_subset_name";
    String SERVICE_INFO_VIRTUAL_SERVICE_HOST_HEADERS = "t_service_info_virtual_service_host_headers";
    String SERVICE_INFO_VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY = "t_service_info_virtual_service_plugin_match_priority";

    /**
     * MATCH级別插件
     */
    String API_MATCH_PLUGINS = "t_api_match_plugins";

    /**
     * API级别插件
     */
    String API_API_PLUGINS = "t_api_api_plugins";

    /**
     * HOST级别插件
     */
    String API_HOST_PLUGINS = "t_api_host_plugins";

    /**
     * api请求uri
     */
    String API_REQUEST_URIS = "t_api_request_uris";

    /**
     * api请求方法
     */
    String API_METHODS = "t_api_methods";

    /**
     * api请求header
     */
    String API_HEADERS = "t_api_headers";

    /**
     * api请求query params
     */
    String API_QUERY_PARAMS = "t_api_query_params";


    /**
     * DestinationRule
     **/

    String DESTINATION_RULE_NAME = "t_destination_rule_name";
    String DESTINATION_RULE_HOST = "t_destination_rule_host";
    String DESTINATION_RULE_CONSECUTIVE_ERRORS = "t_destination_rule_consecutive_errors";
    String DESTINATION_RULE_BASE_EJECTION_TIME = "t_destination_rule_base_ejection_time";
    String DESTINATION_RULE_MAX_EJECTION_PERCENT = "t_destination_rule_max_ejection_percent";
    String DESTINATION_RULE_MIN_HEALTH_PERCENT = "t_destination_rule_min_health_percent";
    String DESTINATION_RULE_PATH = "t_destination_rule_path";
    String DESTINATION_RULE_TIMEOUT = "t_destination_rule_timeout";
    String DESTINATION_RULE_EXPECTED_STATUSES = "t_destination_rule_expected_statuses";
    String DESTINATION_RULE_HEALTHY_INTERVAL = "t_destination_rule_healthy_interval";
    String DESTINATION_RULE_HEALTHY_THRESHOLD = "t_destination_rule_healthy_threshold";
    String DESTINATION_RULE_UNHEALTHY_INTERVAL = "t_destination_rule_unhealthy_interval";
    String DESTINATION_RULE_UNHEALTHY_THRESHOLD = "t_destination_rule_unhealthy_threshold";
    String DESTINATION_RULE_HEALTHY_CHECKER_TYPE = "t_destination_rule_healthy_checker_type";
    String DESTINATION_RULE_ALT_STAT_NAME = "t_destination_rule_alt_stat_name";

    /**
     * 负载均衡相关
     */
    String DESTINATION_RULE_LOAD_BALANCER = "t_destination_rule_load_balancer";
    String DESTINATION_RULE_LOAD_BALANCER_SIMPLE = "t_destination_rule_load_balancer_simple";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH = "t_destination_rule_load_balancer_consistentHash";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE = "t_destination_rule_load_balancer_consistentHash_cookie";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_NAME = "t_destination_rule_load_balancer_consistentHash_cookie_name";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_TTL = "t_destination_rule_load_balancer_consistentHash_cookie_ttl";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_PATH = "t_destination_rule_load_balancer_consistentHash_cookie_path";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_HEADER = "t_destination_rule_load_balancer_consistentHash_header";
    String DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_SOURCEIP = "t_destination_rule_load_balancer_consistentHash_useSourceIp";


    /**
     * 连接池相关
     */
    String DESTINATION_RULE_CONNECTION_POOL = "t_destination_rule_connection_pool";
    String DESTINATION_RULE_HTTP_CONNECTION_POOL = "t_destination_rule_http_connection_pool";
    String DESTINATION_RULE_HTTP_CONNECTION_POOL_HTTP1MAXPENDINGREQUESTS = "t_destination_rule_http_connection_pool_http1MaxPendingRequests";
    String DESTINATION_RULE_HTTP_CONNECTION_POOL_HTTP2MAXREQUESTS = "t_destination_rule_http_connection_pool_http2MaxRequests";
    String DESTINATION_RULE_HTTP_CONNECTION_POOL_MAXREQUESTSPERCONNECTION = "t_destination_rule_http_connection_pool_maxRequestsPerConnection";
    String DESTINATION_RULE_HTTP_CONNECTION_POOL_IDLETIMEOUT = "t_destination_rule_http_connection_pool_idleTimeout";

    String DESTINATION_RULE_TCP_CONNECTION_POOL = "t_destination_rule_tcp_connection_pool";
    String DESTINATION_RULE_TCP_CONNECTION_POOL_MAX_CONNECTIONS = "t_destination_rule_tcp_connection_pool_max_connections";
    String DESTINATION_RULE_TCP_CONNECTION_POOL_CONNECT_TIMEOUT = "t_destination_rule_tcp_connection_pool_connect_timeout";

    /**
     * subset相关
     */
    String DESTINATION_RULE_EXTRA_SUBSETS = "t_destination_rule_extra_subsets";

    String API_GATEWAYS = "t_api_gateways";



    /**
     * ServiceEntry
     **/
    String SERVICE_ENTRY_NAME = "t_service_entry_name";
    String SERVICE_ENTRY_HOST = "t_service_entry_host";
    String SERVICE_ENTRY_PROTOCOL = "t_service_entry_protocol";
    String SERVICE_ENTRY_PROTOCOL_NAME = "t_service_entry_protocol_name";
    String SERVICE_ENTRY_PROTOCOL_PORT = "t_service_entry_protocol_port";

    /**
     * Gateway
     **/
    String GATEWAY_NAME = "t_gateway_name";
    String GATEWAY_NS = "t_gateway_ns";
    String GATEWAY_HOSTS = "t_gateway_hosts";
    String GATEWAY_HTTP_10 = "t_gateway_http_10";
    String GATEWAY_GW_CLUSTER = "t_api_gateway";
    String GATEWAY_CUSTOM_IP_HEADER = "t_custom_ip_header";
    String GATEWAY_XFF_NUM_TRUSTED_HOPS = "t_xff_num_trusted_hops";
    String GATEWAY_USE_REMOTE_ADDRESS = "t_use_remote_address";



    /**
     * SharedConfig
     **/
    String SHARED_CONFIG_DESCRIPTOR = "t_shared_config_descriptor";

    String SHARED_CONFIG_NAME = "t_shared_config_name";
    String SHARED_CONFIG_NAMESPACE = "t_shared_config_namespace";

    /**
     * PluginManager
     **/
    String PLUGIN_MANAGER_NAME = "t_plugin_manager_name";
    String PLUGIN_MANAGER_WORKLOAD_LABELS = "t_plugin_manager_workload_labels";
    String PLUGIN_MANAGER_PLUGINS = "t_plugin_manager_plugins";

    /**
     * VersionManager
     **/
    String VERSION_MANAGER_WORKLOADS = "t_version_manager_workloads";

    /**
     * GatewayPlugin
     */
    String GATEWAY_PLUGIN_NAME = "t_gateway_plugin_name";
    String GATEWAY_PLUGIN_NAMESPACE = "t_gateway_plugin_namespace";
    String GATEWAY_PLUGIN_GATEWAYS = "t_gateway_plugin_gateways";
    String GATEWAY_PLUGIN_HOSTS = "t_gateway_plugin_hosts";
    String GATEWAY_PLUGIN_SERVICES = "t_gateway_plugin_services";
    String GATEWAY_PLUGIN_PLUGINS = "t_gateway_plugin_plugins";
    String GATEWAY_PLUGIN_ROUTES = "t_gateway_plugin_routes";
    String GATEWAY_PLUGIN_USERS = "t_gateway_plugin_users";


    /**
     * SmartLimiter
     */

    String SMART_LIMITER_NAME = "t_smart_limiter_name";
    String SMART_LIMITER_CONFIG = "t_smart_limiter_config";


    /**
     * Sidecar
     */
    String SIDECAR_SOURCE_APP = "t_sidecar_source_app";
    String SIDECAR_EGRESS_HOSTS = "t_sidecar_egress_hosts";

    /**
     * rls = rate limit server
     * rls configmap
     */
    String RLS_CONFIG_MAP_NAME = "t_rls_cm_name";
    String RLS_CONFIG_MAP_DESCRIPTOR = "t_rls_cm_descriptor";


    /** ---------- 模板占位符名 end ---------- **/

    /**
     *
     */

}
