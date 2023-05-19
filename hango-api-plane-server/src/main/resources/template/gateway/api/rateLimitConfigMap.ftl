apiVersion: v1
kind: ConfigMap
metadata:
  name: ${t_shared_config_name}
  namespace: ${t_namespace}
data:
  config.yaml: |-
<#list t_shared_config_descriptor as d>
<@indent count=4>${d}</@indent>
</#list>
