apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
<#include "common/metadata.ftl"/>
spec:
  host: ${nsfExtra.host!}
  trafficPolicy:
    outlierDetection:
      consecutiveErrors: ${nsfExtra.consecutiveErrors!}
      interval: ${nsfExtra.interval!}
      baseEjectionTime: ${nsfExtra.baseEjectionTime!}
      maxEjectionPercent: ${nsfExtra.maxEjectionPercent!}