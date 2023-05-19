apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ${t_envoy_filter_name}
spec:
<#if t_envoy_filter_workload_labels??>
  workloadSelector:
    labels:
<#list t_envoy_filter_workload_labels as k,v>
      ${k}: ${v}
</#list>
</#if>


<#if t_envoy_filter_filters??>
  configPatches:
<#list t_envoy_filter_filters as f>
  -
<@indent count=4>${f}</@indent>
</#list>
</#if>
