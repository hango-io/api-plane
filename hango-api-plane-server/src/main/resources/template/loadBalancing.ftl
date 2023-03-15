apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
<#include "common/metadata.ftl"/>
spec:
  host: ${nsfExtra.host!}
  trafficPolicy:
    loadBalancer:
      simple: ${nsfExtra.simple!}
