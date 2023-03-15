apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${t_virtual_service_name}
  labels:
    api_service: ${t_api_service}
<#if t_version?has_content>
    hango-data-version: ${t_version}
</#if>
spec:
  gateways:
  - ${t_gateway_name}
  hosts:
<#list t_virtual_service_hosts as host>
  - "${host}"
</#list>
<#if t_virtual_service_virtual_cluster_name?has_content>
  virtualCluster:
  - headers:
<#if t_virtual_service_virtual_cluster_headers?has_content>
<#list t_virtual_service_virtual_cluster_headers as h>
      ${h.key}:
        ${h.type}: "${h.value?j_string}"
</#list>
</#if>
    name: ${t_virtual_service_virtual_cluster_name}
</#if>
<#if t_api_host_plugins?has_content>
<#list t_api_host_plugins as p>
<@indent count=2>${p}</@indent>
</#list>
</#if>
  http:
<#list t_api_match_plugins as p>
<@indent count=2><@supply>${p}</@supply></@indent>
</#list>
<@indent count=2><@supply></@supply></@indent>
<#if t_api_api_plugins?has_content>
  plugins:
    ${t_api_name}:
      userPlugin:
<#list t_api_api_plugins?keys as userId>
      -
<#if userId?has_content>
        user: ${userId}
</#if>
<#list t_api_api_plugins[userId] as p>
<@indent count=8>${p}</@indent>
</#list>
</#list>
</#if>
<#if t_api_priority??>
  priority: ${t_api_priority}
</#if>



