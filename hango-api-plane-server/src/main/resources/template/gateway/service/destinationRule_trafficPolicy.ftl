trafficPolicy:
<#if t_destination_rule_connection_pool?has_content>
  connectionPool:
    <#if t_destination_rule_tcp_connection_pool?has_content>
    tcp:
      <#if t_destination_rule_tcp_connection_pool_max_connections?has_content>
      maxConnections: ${t_destination_rule_tcp_connection_pool_max_connections}
      </#if>
      <#if t_destination_rule_tcp_connection_pool_connect_timeout?has_content>
      connectTimeout: ${t_destination_rule_tcp_connection_pool_connect_timeout}ms
      </#if>
    </#if>
    <#if t_destination_rule_http_connection_pool?has_content>
    http:
      <#if t_destination_rule_http_connection_pool_http1MaxPendingRequests?has_content>
      http1MaxPendingRequests: ${t_destination_rule_http_connection_pool_http1MaxPendingRequests}
      </#if>
      <#if t_destination_rule_http_connection_pool_http2MaxRequests?has_content>
      http2MaxRequests: ${t_destination_rule_http_connection_pool_http2MaxRequests}
      </#if>
      <#if t_destination_rule_http_connection_pool_maxRequestsPerConnection?has_content>
      maxRequestsPerConnection: ${t_destination_rule_http_connection_pool_maxRequestsPerConnection}
      </#if>
      <#if t_destination_rule_http_connection_pool_idleTimeout?has_content>
      idleTimeout: ${t_destination_rule_http_connection_pool_idleTimeout}ms
      </#if>
    </#if>
</#if>
<#if t_destination_rule_load_balancer?has_content>
  loadBalancer:
    <#if t_destination_rule_locality_enable?has_content>
    localityLbSetting:
      enabled: ${t_destination_rule_locality_enable}
    </#if>
    <#if t_destination_rule_load_balancer_simple?has_content>
    simple: ${t_destination_rule_load_balancer_simple}
    </#if>
    <#if t_destination_rule_load_balancer_slow_start_window?has_content>
    warmupDurationSecs: ${t_destination_rule_load_balancer_slow_start_window}s
    </#if>
    <#if t_destination_rule_load_balancer_consistentHash?has_content>
    consistentHash:
      <#if t_destination_rule_load_balancer_consistentHash_header?has_content>
      httpHeaderName: "${t_destination_rule_load_balancer_consistentHash_header}"
      </#if>
      <#if t_destination_rule_load_balancer_consistentHash_useSourceIp??>
      useSourceIp: ${t_destination_rule_load_balancer_consistentHash_useSourceIp?string}
      </#if>
      <#if t_destination_rule_load_balancer_consistentHash_cookie?has_content>
      httpCookie:
        <#if t_destination_rule_load_balancer_consistentHash_cookie_name?has_content>
        name: ${t_destination_rule_load_balancer_consistentHash_cookie_name}
        </#if>
        <#if t_destination_rule_load_balancer_consistentHash_cookie_ttl?has_content>
        ttl: ${t_destination_rule_load_balancer_consistentHash_cookie_ttl}s
        </#if>
        <#if t_destination_rule_load_balancer_consistentHash_cookie_path?has_content>
        path: ${t_destination_rule_load_balancer_consistentHash_cookie_path}
        </#if>
      </#if>
    </#if>
</#if>
<#--loadBalancer:-->
<#--<#if t_destination_rule_load_balancer?has_content>-->
<#--<@indent count=4>${t_destination_rule_load_balancer}</@indent>-->
<#--</#if>-->
  outlierDetection:
<#if t_destination_rule_consecutive_errors?has_content>
    consecutive5xxErrors: ${t_destination_rule_consecutive_errors}
</#if>
<#if t_destination_rule_base_ejection_time?has_content>
    baseEjectionTime: ${t_destination_rule_base_ejection_time}s
</#if>
<#if t_destination_rule_max_ejection_percent?has_content>
    maxEjectionPercent: ${t_destination_rule_max_ejection_percent}
</#if>
<#if t_destination_rule_min_health_percent?has_content>
    minHealthPercent: ${t_destination_rule_min_health_percent}
</#if>
  healthCheck:
  <#if t_destination_rule_path?has_content>
    healthChecker:
    <#if t_destination_rule_healthy_checker_type == "http">
      httpHealthCheck:
      <#if t_destination_rule_path?has_content || t_destination_rule_expected_statuses?has_content>
        host: ${t_destination_rule_host}
      </#if>
      <#if t_destination_rule_path?has_content>
        path: ${t_destination_rule_path}
      </#if>
      <#if t_destination_rule_expected_statuses?has_content>
        expectedStatuses:
        <#list t_destination_rule_expected_statuses as s>
        - start: ${s}
          end: ${s+1}
      </#list>
    </#if>
    </#if>
  </#if>
<#if t_destination_rule_timeout?has_content>
    timeout: ${t_destination_rule_timeout}ms
</#if>
<#if t_destination_rule_healthy_interval?has_content>
    interval: ${t_destination_rule_healthy_interval}s
</#if>
<#if t_destination_rule_healthy_threshold?has_content>
    healthyThreshold: ${t_destination_rule_healthy_threshold}
</#if>
<#if t_destination_rule_unhealthy_interval?has_content>
    unhealthyInterval: ${t_destination_rule_unhealthy_interval}s
</#if>
<#if t_destination_rule_unhealthy_threshold?has_content>
    unhealthyThreshold: ${t_destination_rule_unhealthy_threshold}
</#if>
