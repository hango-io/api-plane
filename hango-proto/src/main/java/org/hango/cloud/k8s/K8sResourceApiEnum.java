package org.hango.cloud.k8s;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:39
**/
@SuppressWarnings("java:S115")
public enum K8sResourceApiEnum {
    VirtualService("virtualservices.networking.istio.io"),
    DestinationRule("destinationrules.networking.istio.io"),
    EnvoyPlugin("envoyplugins.microservice.slime.io"),
    KubernetesGateway("gateways.gateway.networking.k8s.io"),
    SmartLimiter("smartlimiters.microservice.slime.io"),
    HTTPRoute("httproutes.gateway.networking.k8s.io");

    String api;
    K8sResourceApiEnum(String api){
        this.api = api;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public static K8sResourceApiEnum getByName(String name){
        for (K8sResourceApiEnum value : values()) {
            if (value.name().equals(name)){
                return value;
            }
        }
        return null;
    }

}
