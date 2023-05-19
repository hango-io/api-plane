apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
<#include "common/metadata.ftl"/>
spec:
  hosts:
  - ${nsfExtra.host!}
  http:
  - match:
    - headers:
    <#list nsfExtra.headers! as header>
        ${header.header}:
          ${header.headerMatch?keys[0]}: ${header.headerMatch?values[0]}
      </#list>
    <#if nsfExtra.uri ??>
      uri:
        ${nsfExtra.uri.urlMatch?keys[0]}: ${nsfExtra.uri.urlMatch?values[0]}
    </#if>
  - route:
    <#list nsfExtra.destinations! as des>
    - destination:
        host: ${nsfExtra.host!}
        subset: ${des.subset!}
      weight: ${des.weight!}
    </#list>