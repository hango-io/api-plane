apiVersion: microservice.netease.com/v1alpha1
kind: SmartLimiter
metadata:
  name: ${t_smart_limiter_name}
  namespace: ${t_namespace}
spec:
<#if t_smart_limiter_config?has_content>
  ratelimitConfig:
    rate_limit_conf:
<@indent count=6>${t_smart_limiter_config}</@indent>
</#if>