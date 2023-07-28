package org.hango.cloud.service;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import org.hango.cloud.meta.dto.IngressDTO;
import org.hango.cloud.meta.dto.KubernetesGatewayDTO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/12/2
 */
public interface KubernetesGatewayService {

    /**
     * 获取k8s gateway资源
     * @param gateway
     * @return
     */
    List<KubernetesGatewayDTO> getKubernetesGateway(String gateway);

    /**
     * 获取gateay api中的httproute资源
     * @param gateway
     * @return
     */
    List<HTTPRoute> getHTTPRoute(String gateway);

    /**
     * 获取ingress列表
     * @param namespace 命名空间
     * @param name ingress name
     */
    List<IngressDTO> getIngress(String namespace, String name);

}
