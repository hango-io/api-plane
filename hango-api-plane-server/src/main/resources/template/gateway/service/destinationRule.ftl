apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${t_destination_rule_name}
spec:
  host: ${t_destination_rule_host}
<#--- 默认所有subset都继承同一份trafficPolicy --->
  subsets:
<#--- 默认生成的subset --->
  - name: ${t_api_service}-${t_api_gateway}
<@indent count=4><@autoremove><#include "destinationRule_trafficPolicy.ftl"/></@autoremove></@indent>
<#--- 自定义的subset --->
<#if t_destination_rule_extra_subsets?has_content>
<#list t_destination_rule_extra_subsets as ss>
  - name: ${ss.name}
<#if ss.labels?has_content>
    labels:
<#list ss.labels?keys as k>
      ${k}: ${ss.labels[k]}
</#list>
</#if>
    <#if ss.trafficPolicy?has_content>
    trafficPolicy:
      tls:
        mode: DISABLE
      <#if ss.trafficPolicy.loadBalancer?has_content>
      loadBalancer:
        <#if ss.trafficPolicy.loadBalancer.simple?has_content>
        simple: ${ss.trafficPolicy.loadBalancer.simple}
        </#if>
        <#if ss.trafficPolicy.loadBalancer.consistentHash?has_content>
        consistentHash:
          <#if ss.trafficPolicy.loadBalancer.consistentHash.httpHeaderName?has_content>
          httpHeaderName: ${ss.trafficPolicy.loadBalancer.consistentHash.httpHeaderName}
          </#if>
          <#if ss.trafficPolicy.loadBalancer.consistentHash.useSourceIp??>
          useSourceIp: ${ss.trafficPolicy.loadBalancer.consistentHash.useSourceIp?string}
          </#if>
          <#if ss.trafficPolicy.loadBalancer.consistentHash.httpCookie?has_content>
          httpCookie:
            <#if ss.trafficPolicy.loadBalancer.consistentHash.httpCookie.name?has_content>
            name: ${ss.trafficPolicy.loadBalancer.consistentHash.httpCookie.name}
            </#if>
            <#if ss.trafficPolicy.loadBalancer.consistentHash.httpCookie.ttl?has_content>
            ttl: ${ss.trafficPolicy.loadBalancer.consistentHash.httpCookie.ttl}s
            </#if>
            <#if ss.trafficPolicy.loadBalancer.consistentHash.httpCookie.path?has_content>
            path: ${ss.trafficPolicy.loadBalancer.consistentHash.httpCookie.path}
            </#if>
          </#if>
        </#if>
      </#if>

      <#if ss.trafficPolicy.connectionPool?has_content>
      connectionPool:
        <#if ss.trafficPolicy.connectionPool.tcp?has_content>
        tcp:
          <#if ss.trafficPolicy.connectionPool.tcp.maxConnections?has_content>
          maxConnections: ${ss.trafficPolicy.connectionPool.tcp.maxConnections}
          </#if>
          <#if ss.trafficPolicy.connectionPool.tcp.connectTimeout?has_content>
          connectTimeout: ${ss.trafficPolicy.connectionPool.tcp.connectTimeout}ms
          </#if>
        </#if>
        <#if ss.trafficPolicy.connectionPool.http?has_content>
        http:
          <#if ss.trafficPolicy.connectionPool.http.http1MaxPendingRequests?has_content>
          http1MaxPendingRequests: ${ss.trafficPolicy.connectionPool.http.http1MaxPendingRequests}
          </#if>
          <#if ss.trafficPolicy.connectionPool.http.http2MaxRequests?has_content>
          http2MaxRequests: ${ss.trafficPolicy.connectionPool.http.http2MaxRequests}
          </#if>
          <#if ss.trafficPolicy.connectionPool.http.maxRequestsPerConnection?has_content>
          maxRequestsPerConnection: ${ss.trafficPolicy.connectionPool.http.maxRequestsPerConnection}
          </#if>
          <#if ss.trafficPolicy.connectionPool.http.idleTimeout?has_content>
          idleTimeout: ${ss.trafficPolicy.connectionPool.http.idleTimeout}ms
          </#if>
        </#if>
      </#if>

      <#if ss.trafficPolicy.outlierDetection?has_content>
      outlierDetection:
        <#if ss.trafficPolicy.outlierDetection.consecutiveErrors?has_content>
        consecutiveErrors: ${ss.trafficPolicy.outlierDetection.consecutiveErrors}
        </#if>
        <#if ss.trafficPolicy.outlierDetection.baseEjectionTime?has_content>
        baseEjectionTime: ${ss.trafficPolicy.outlierDetection.baseEjectionTime}ms
        </#if>
        <#if ss.trafficPolicy.outlierDetection.maxEjectionPercent?has_content>
        maxEjectionPercent: ${ss.trafficPolicy.outlierDetection.maxEjectionPercent}
        </#if>
      </#if>
    </#if>
    <#--<@indent count=4><@autoremove><#include "destinationRule_subset_trafficPolicy.ftl"/></@autoremove></@indent>-->
</#list>

</#if>