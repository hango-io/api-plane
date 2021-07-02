<#if t_virtual_service_request_headers?has_content>
headers:
  request:
    <#assign addMap = t_virtual_service_request_headers.add/>
    add:
    <#list addMap as k,v>
      ${k}: "${v}"
    </#list>
</#if>