apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${t_gateway_name}
spec:
  selector:
    gw_cluster: ${t_api_gateway}
  servers:
  - port:
      name: http
      number: 80
      protocol: HTTP
    hosts:
      - "*"