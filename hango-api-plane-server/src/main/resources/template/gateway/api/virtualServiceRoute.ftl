<#if t_virtual_service_destinations?has_content>
route:
<#list t_virtual_service_destinations as ds>
- destination:
    host: ${ds.host}
    port:
      number: ${ds.port?c}
    subset: ${ds.subset}
  weight: ${ds.weight}
</#list>
</#if>