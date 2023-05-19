<#if t_api_connect_timeout??>
timeout: ${t_api_connect_timeout?c}ms
</#if>
<#if t_api_retries??>
retries:
  attempts: ${t_api_retries?c}
</#if>
<#if t_api_preserve_host??>
<#if !t_api_preserve_host>
headers:
  remove: Host
</#if>
</#if>