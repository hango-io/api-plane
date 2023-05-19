metadata:
  proxy.filters.http.metadatahub:
<#if t_gateway_ns?has_content>
    qz_cluster_ns: ${t_gateway_ns}
</#if>
<#if t_virtual_service_metadata_hub?has_content>
  <#list t_virtual_service_metadata_hub?keys as tagKey>
    "${tagKey}": "${t_virtual_service_metadata_hub[tagKey]}"
  </#list>
</#if>

<#if t_virtual_service_stats?has_content>
  proxy.metadata_stats.detailed_stats:
    stat_prefix: detailed_route
    stat_tags:
  <#list t_virtual_service_stats?keys as tagKey>
    - key: "${tagKey}"
      val: "${t_virtual_service_stats[tagKey]}"
  </#list>
</#if>

<#if t_virtual_service_dubbo_meta_service?has_content >
  proxy.upstreams.http.dubbo:
    <#if t_virtual_service_resp_exception_code??>
    resp_exception_code: ${t_virtual_service_resp_exception_code}
    </#if>
    context:
      service: "${t_virtual_service_dubbo_meta_service?j_string}"
      version: "${t_virtual_service_dubbo_meta_version?j_string}"
      method: "${t_virtual_service_dubbo_meta_method?j_string}"
      group: "${t_virtual_service_dubbo_meta_group?j_string}"
      source: ${(t_virtual_service_dubbo_meta_source=="body")?string('HTTP_BODY','HTTP_QUERY')}
      <#if t_virtual_service_dubbo_meta_params?has_content>
      parameters:
      <#list t_virtual_service_dubbo_meta_params as p>
      - type: "${p.value?j_string}"
        name: "${p.key?j_string}"
        required: ${p.required?c}
        <#if p.defaultValue ??>
        default: '${p.defaultJsonValue}'
        </#if>
        <#if p.genericMap?has_content>
        generic:
        <#list p.genericMap as k,v>
        - path: "${k?j_string}"
          type: "${v?j_string}"
        </#list>
        </#if>
      </#list>
      </#if>
      <#if t_virtual_service_dubbo_meta_attachments?has_content>
      attachments:
      <#list t_virtual_service_dubbo_meta_attachments as a>
      - name: "${a.serverParamName?j_string}"
      <#if a.paramPosition == "Header">
        header: "${a.clientParamName?j_string}"
      <#elseif a.paramPosition == "Cookie">
        cookie: "${a.clientParamName?j_string}"
      </#if>
      </#list>
      </#if>
      ignore_null_map_pair: false
</#if>