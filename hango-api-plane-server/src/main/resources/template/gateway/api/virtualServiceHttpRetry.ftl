<#if t_http_retry_retryOn?has_content>
retries:
  attempts: ${t_http_retry_attempts}
  perTryTimeout: ${t_http_retry_perTryTimeout}ms
  retryOn: ${t_http_retry_retryOn}
</#if>