package org.hango.cloud.service;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import org.hango.cloud.meta.dto.KubernetesGatewayDTO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/12/2
 */
public interface KubernetesGatewayService {

    List<KubernetesGatewayDTO> getKubernetesGateway(String gateway);

    List<HTTPRoute> getHTTPRoute(String gateway);

}
