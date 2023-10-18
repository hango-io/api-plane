apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${t_gateway_name}
spec:
  selector:
    gw_cluster: ${t_api_gateway}
<#if t_gateway_servers?has_content>
  servers:
<#list t_gateway_servers as ss>
    - hosts:
     <#list ss.hosts as h>
      - "${h?j_string}"
     </#list>
      port:
        name: ${ss.name}
        number: ${ss.number}
        protocol: ${ss.protocol}
<#if ss.istioGatewayTLS?has_content>
      tls:
        credentialName: "${ss.istioGatewayTLS.credentialName?j_string}"
        mode: ${ss.istioGatewayTLS.mode}
</#if>
</#list>
</#if>
