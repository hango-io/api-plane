apiVersion: networking.istio.io/v1alpha3
kind: GatewayPlugin
metadata:
  name: ${t_gateway_plugin_name}
<#if t_gateway_plugin_namespace?has_content>
  namespace: ${t_gateway_plugin_namespace}
</#if>
spec:
  gateway:
  - mesh
<#if t_gateway_plugin_hosts?has_content>
  host:
<#list t_gateway_plugin_hosts as h>
  - ${h}
</#list>
</#if>
<#if t_gateway_plugin_routes?has_content>
  route:
<#list t_gateway_plugin_routes as r>
  - ${r}
</#list>
</#if>
<#if t_gateway_plugin_users?has_content>
  user:
<#list t_gateway_plugin_users as u>
  - ${u}
</#list>
</#if>
<#if t_gateway_plugin_plugins?has_content>
  plugins:
    <#list t_gateway_plugin_plugins as p>
    -
<@indent count=6>${p}</@indent>
    </#list>
</#if>