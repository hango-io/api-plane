# 严选TLS+分流, 使用com.netease.cloud.nsf.meta.WhiteList进行填充
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${service}
  namespace: ${namespace}
spec:
  hosts:
  - ${service}
  http:
  - route:
    - destination:
        host: ${service}
      weight: 100
    - destination:
        host: qz-egress.istio-system.svc.cluster.local
      weight: 0
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${service}
  namespace: ${namespace}
spec:
  host: ${service}
  trafficPolicy:
    tls:
      mode: ISTIO_MUTUAL
  host: ${service}
---
apiVersion: "authentication.istio.io/v1alpha1"
kind: "Policy"
metadata:
  name: ${service}
  namespace: ${namespace}
spec:
  targets:
  - name: ${service}
  peers:
  - mtls:
      mode: STRICT
