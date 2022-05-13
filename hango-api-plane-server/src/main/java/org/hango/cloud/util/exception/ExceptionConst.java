package org.hango.cloud.util.exception;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
public interface ExceptionConst {

    String ISTIO_POD_NON_EXIST = "Istio pod is non-exist";
    String PILOT_SERVICE_NON_EXIST = "Pilot service is non-exist";
    String GALLEY_SERVICE_NON_EXIST = "Galley service is non-exist";
    String ENVOY_POD_NON_EXIST = "Envoy pod is non-exist";
    String RESOURCE_NON_EXIST = "Resource is non-exist";
    String K8S_SERVICE_NON_EXIST = "k8s service is non-exist";
    String SERVICE_NON_EXIST = "service is non-exist";
    String API_NON_EXIST = "api is non-exist";
    String RESOURCE_KIND_MISMATCH = "Resource kind is mismatch";
    String RESOURCES_DIFF_IDENTITY = "Resources have different identities";
    String GATEWAY_LIST_EMPTY = "gateway list is empty";
    String PROXY_URI_LIST_EMPTY = "proxy uri list is empty";

    String TARGET_SERVICE_NON_EXIST = "target service is non-exist";

    String ENDPOINT_LIST_EMPTY = "endpoint list is empty";

    String UNSUPPORTED_RESOURCE_TYPE = "unsupported resource type";

    String UNSUPPORTED_OPERATION = "unsupported operation";
}
