apiVersion: microservice.slime.io/v1alpha1
kind: EnvoyPlugin
metadata:
  name: ${t_gateway_plugin_name}
  labels:
<#if t_version?has_content>
    hango-data-version: ${t_version}
</#if>
<#if t_gateway_plugin_namespace?has_content>
  namespace: ${t_gateway_plugin_namespace}
</#if>
spec:
<#if t_gateway_plugin_gateways?has_content>
  gateway:
  <#list t_gateway_plugin_gateways as g>
  - ${g}
  </#list>
</#if>
<#if t_gw_cluster?has_content>
  workloadSelector:
    labels:
      gw_cluster: ${t_gw_cluster}
</#if>
<#if t_gateway_plugin_hosts?has_content>
  host:
<#list t_gateway_plugin_hosts as h>
  - "${h?j_string}"
</#list>
</#if>
<#if t_gateway_plugin_route?has_content>
  route:
  - "${t_gateway_plugin_route}"
</#if>
<#if t_gateway_plugin_plugins?has_content>
  plugins:
    <#list t_gateway_plugin_plugins as p>
    -
<@indent count=6>${p}</@indent>
    </#list>
</#if>