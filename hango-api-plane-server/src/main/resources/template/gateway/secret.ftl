apiVersion: v1
kind: Secret
metadata:
  name: ${t_secret_name}
data:
<#if t_secret_tls_crt ??>
  tls.crt: ${t_secret_tls_crt}
</#if>
<#if t_secret_tls_key ??>
  tls.key: ${t_secret_tls_key}
</#if>
<#if t_secret_ca_crt ??>
  ca.crt: ${t_secret_ca_crt}
</#if>
type: kubernetes.io/tls