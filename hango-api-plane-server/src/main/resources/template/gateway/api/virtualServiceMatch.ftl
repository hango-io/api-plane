match:
- uri:
    ${t_virtual_service_url_match}: "${t_api_request_uris}"
<#if t_api_methods?has_content>
  method:
    regex: ${t_api_methods}
</#if>
<#if t_virtual_service_host_headers?has_content || t_api_headers?has_content>
  headers:
<#if t_virtual_service_host_headers?has_content>
    :authority:
      regex: ${t_virtual_service_host_headers}
</#if>
<#if t_api_headers?has_content>
  <#list t_api_headers as h>
    ${h.key}:
      ${h.type}: ${h.value}
  </#list>
</#if>
</#if>
<#if t_api_query_params?has_content>
  queryParams:
<#list t_api_query_params as p>
    ${p.key}:
      ${p.type}: ${p.value}
</#list>
</#if>
<#if t_virtual_service_timeout??>
timeout: ${t_virtual_service_timeout}ms
</#if>
<@autoremove><#include "virtualServiceHeaderOperation.ftl"/></@autoremove>