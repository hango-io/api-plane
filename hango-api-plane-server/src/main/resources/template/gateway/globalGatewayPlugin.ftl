<#-- 网关/项目级别全局插件 -->
apiVersion: microservice.slime.io/v1alpha1
kind: EnvoyPlugin
metadata:
  name: ${t_gateway_plugin_name}
spec:
<#if t_gateway_plugin_gateways?has_content>
  workloadSelector:
    labels:
      gw_cluster: ${t_gateway_plugin_gateways}
</#if>
<#if t_gateway_plugin_hosts?has_content>
  host:
  <#list t_gateway_plugin_hosts as h>
    - ${h}
  </#list>
</#if>
<#if t_gateway_plugin_plugins??>
  plugins:
  <#list t_gateway_plugin_plugins as p>
    -
    <@indent count=6>${p}</@indent>
  </#list>
</#if>