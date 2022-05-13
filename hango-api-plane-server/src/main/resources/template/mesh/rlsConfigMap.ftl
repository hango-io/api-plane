apiVersion: v1
kind: ConfigMap
metadata:
  name: ${t_rls_cm_name}
  namespace: ${t_namespace}
data:
  config.yaml: |-
<#list t_rls_cm_descriptor as d>
<@indent count=4>${d}</@indent>
</#list>
