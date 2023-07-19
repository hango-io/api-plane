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
  ${t_virtual_service_protocol}:
<@indent count=2><@supply></@supply></@indent>
<#if t_api_priority??>
  priority: ${t_api_priority}
</#if>



