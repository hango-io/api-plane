apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: ${t_service_entry_name}
spec:
  endpoints:
<#list endpoints as e>
  - address: ${e.address}
    labels:
      gw_cluster: ${t_api_gateway}
<#if e.labels ??>
<#list e.labels as k,v>
      ${k}: ${v}
</#list>
</#if>
<#if e.port ??>
    ports:
      ${t_service_entry_protocol_name}: ${e.port?c}
</#if>
</#list>
  exportTo:
  - "*"
  hosts:
  - ${t_service_entry_host}
  location: MESH_EXTERNAL
  ports:
  - name: ${t_service_entry_protocol_name}
    number: ${t_service_entry_protocol_port}
    protocol: ${t_service_entry_protocol}
  resolution: DNS