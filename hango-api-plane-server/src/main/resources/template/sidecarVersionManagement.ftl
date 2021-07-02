apiVersion: networking.istio.io/v1alpha3
kind: VersionManager
metadata:
  name: version-manager
  namespace: ${t_namespace}
spec:
  defaultVersion: envoy
  retryPolicy:
    neverRetry: false
    retryTime: 5
    retryInterval: 3s
  sidecarVersionSpec:
<#list t_version_manager_workloads! as w>
  - podsHash: none
    <#if w.expectedVersion?? >
    expectedVersion: ${w.expectedVersion}
    </#if>
    <#if w.iptablesParams?? >
    iptablesParams: ${w.iptablesParams}
    </#if>
    <#if w.iptablesDetail?? >
    iptablesDetail: '${w.iptablesDetail}'
    </#if>
    <#if w.workLoadType == "Deployment">
    viaDeployment:
      name: ${w.workLoadName}
    </#if>
    <#if w.workLoadType == "StatefulSet">
    viaStatefulSet:
      name: ${w.workLoadName}
    </#if>
    <#if w.workLoadType == "Service">
    viaService:
      name: ${w.workLoadName}
    </#if>
    <#if w.workLoadType == "LabelSelector">
    viaLabelSelector:
      labels:
      <#list w.labels?keys as key>
        ${key}: ${w.labels[key]!}
      </#list>
    </#if>
</#list>