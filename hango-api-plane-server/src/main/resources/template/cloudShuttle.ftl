# 严选云内外分流
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
<#include "common/metadata.ftl"/>
spec:
  hosts:
  - yx-provider
  http:
  - route:
    - destination:
        host: ${metadata.name}
        subset: internal
      weight: ${100 - nsfExtra.outWeight!0}
    - destination:
        host: qz-egress.qz.svc.cluster.local
      weight: ${nsfExtra.outWeight!0}
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
<#include "common/metadata.ftl"/>
spec:
  host: ${metadata.name}
  subsets:
  - name: internal
    labels:
      app: ${metadata.name}
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: ${metadata.name}-${metadata.namespace}
  namespace: qz
spec:
  rules:
  - services:
    - qz-egress.qz.svc.cluster.local
    methods:
    - GET
    - HEAD
    constraints:
    - key: "request.headers[:authority]"
      values: ["${metadata.name}"]
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: ${metadata.name}-${metadata.namespace}-whitelist
  namespace: qz
spec:
  subjects:
  - user: "*"
  roleRef:
    kind: ServiceRole
    name: ${metadata.name}-${metadata.namespace}
