apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${t_destination_rule_name}
spec:
  host: ${t_destination_rule_host}
  exportTo:
  - "."
  subsets:
  <#list t_api_gateways as gateway>
  - name: ${t_api_service}-${t_api_name}-${gateway}
    api: ${t_api_service}-${t_api_name}
  <#if t_api_loadBalancer ??>
    <#if t_api_loadBalancer != "consistent_hash">
    trafficPolicy:
      loadBalancer:
        simple: ${t_api_loadBalancer?upper_case}
    </#if>
  </#if>
  <#if t_api_idle_timeout??>
    trafficPolicy:
      connectionPool:
        http:
          idleTimeout: ${t_api_idle_timeout?c}ms
  </#if>
  </#list>