apiVersion: microservice.slime.io/v1alpha1
kind: EnvoyPlugin
metadata:
  name: ${t_gateway_plugin_name}
<#if t_gateway_plugin_namespace?has_content>
  namespace: ${t_gateway_plugin_namespace}
</#if>
<@indent count=2><#include "../common/identityLabel.ftl"/></@indent>
spec:
<#if t_gateway_plugin_gateways?has_content>
  gateway:
  <#list t_gateway_plugin_gateways as g>
  - ${g}
  </#list>
</#if>
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
    <#list t_gateway_plugin_plugins as k,plugins>
    <#list plugins as p>
    -
<#if k?has_content>
      user: ${k}
</#if>
<@indent count=6>${p}</@indent>
    </#list>
    </#list>
</#if>