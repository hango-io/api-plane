metadata:
  proxy.filters.http.metadatahub:
<#if t_gateway_name?has_content>
    qz_cluster_name: ${t_gateway_name}
</#if>
<#if t_gateway_ns?has_content>
    qz_cluster_ns: ${t_gateway_ns}
</#if>
<#if t_virtual_service_service_tag?has_content>
    qz_svc_id: ${t_virtual_service_service_tag}
</#if>
<#if t_virtual_service_api_id?has_content>
    qz_api_id: ${t_virtual_service_api_id}
</#if>
<#if t_virtual_service_tenant_id?has_content>
    qz_tenant_id: ${t_virtual_service_tenant_id}
</#if>
<#if t_virtual_service_project_id?has_content>
    qz_project_id: ${t_virtual_service_project_id}
</#if>
<#if t_virtual_service_api_name?has_content>
    qz_api_name: ${t_virtual_service_api_name}
</#if>

<#if t_virtual_service_stats?has_content>
  proxy.filters.http.detailed_stats:
    stats:
  <#list t_virtual_service_stats as stats_meta>
      - ${stats_meta}
  </#list>
</#if>

<#if t_virtual_service_dubbo_meta_service?has_content >
  proxy.upstreams.http.dubbo:
    <#if t_virtual_service_resp_exception_code??>
    resp_exception_code: ${t_virtual_service_resp_exception_code}
    </#if>
    context:
      service: ${t_virtual_service_dubbo_meta_service}
      version: ${t_virtual_service_dubbo_meta_version}
      method: ${t_virtual_service_dubbo_meta_method}
      group: ${t_virtual_service_dubbo_meta_group}
      source: ${(t_virtual_service_dubbo_meta_source=="body")?string('HTTP_BODY','HTTP_QUERY')}
      <#if t_virtual_service_dubbo_meta_params?has_content>
      parameters:
      <#list t_virtual_service_dubbo_meta_params as p>
      - type: ${p.value}
        name: ${p.key}
        required: ${p.required?c}
        <#if p.defaultValue ??>
        default: '${p.defaultJsonValue}'
        </#if>
        <#if p.genericMap?has_content>
        generic:
        <#list p.genericMap as k,v>
        - path: ${k}
          type: ${v}
        </#list>
        </#if>
      </#list>
      </#if>
      <#if t_virtual_service_dubbo_meta_attachments?has_content>
      attachments:
      <#list t_virtual_service_dubbo_meta_attachments as a>
      - name: ${a.serverParamName}
      <#if a.paramPosition == "Header">
        header: ${a.clientParamName}
      <#elseif a.paramPosition == "Cookie">
        cookie: ${a.clientParamName}
      </#if>
      </#list>
      </#if>
      ignore_null_map_pair: false
</#if>