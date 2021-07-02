apiVersion: networking.istio.io/v1alpha3
kind: Sidecar
metadata:
  name: ${t_sidecar_source_app}
  namespace: ${t_namespace}
spec:
  workloadSelector:
    labels:
      app: ${t_sidecar_source_app}
  egress:
  - bind: "0.0.0.0"
    hosts:
    - "istio-system/*"
    - "istio-telemetry/*"
<#if t_sidecar_egress_hosts??>
  <#list t_sidecar_egress_hosts as h>
    - "*/${h}"
  </#list>
</#if>