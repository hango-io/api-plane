#黑白名单，使用com.netease.cloud.nsf.meta.WhiteList进行填充
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: qz-ingress-whitelist
  namespace: ${namespace}
spec:
  rules:
# 仅作占位符
  - services: ["${service}.${namespace}.svc.cluster.local"]
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: qz-ingress-whitelist
  namespace: ${namespace}
spec:
  subjects:
  - user: "cluster.local/ns/istio-system/sa/qz-ingress"
  roleRef:
    kind: ServiceRole
    name: qz-ingress-whitelist
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: qz-ingress-passed
  namespace: ${namespace}
spec:
# 仅作占位符
  rules:
  - services: ["${service}.${namespace}.svc.cluster.local"]
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: qz-ingress-passed
  namespace: ${namespace}
spec:
  subjects:
  - user: "cluster.local/ns/istio-system/sa/qz-ingress"
  roleRef:
    kind: ServiceRole
    name: qz-ingress-passed


