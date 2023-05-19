<#if t_virtual_service_mirror_service?has_content>
mirror:
    host: ${t_virtual_service_mirror_service}
    port:
      number: ${t_virtual_service_mirror_port}
<#if t_virtual_service_mirror_subset??>
    subset: ${t_virtual_service_mirror_subset}
</#if>
</#if>