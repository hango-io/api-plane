apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${t_gateway_name}
spec:
<#if t_gateway_http_10?has_content && t_gateway_http_10>
  enableHttp10: true
  defaultHostForHttp10: netease.com
</#if>
  selector:
    gw_cluster: ${t_api_gateway}
  servers:
  - port:
      name: http
      number: 80
      protocol: HTTP
    hosts:
      - "*"
   <#if t_custom_ip_header ??>
    customIpAddressHeader: ${t_custom_ip_header}
   </#if>
   <#if t_xff_num_trusted_hops ??>
    xffNumTrustedHops: ${t_xff_num_trusted_hops}
   </#if>
   <#if t_use_remote_address ??>
    useRemoteAddress: ${t_use_remote_address}
   </#if>