package org.hango.cloud.web.controller;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import org.hango.cloud.meta.dto.KubernetesGatewayDTO;
import org.hango.cloud.service.KubernetesGatewayService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author zhufengwei
 * @Date 2022/12/2
 */
@RestController
@RequestMapping(value = "/api", params = "Version=2022-12-31")
public class KubernetesGatewayController extends BaseController{

    @Autowired
    private KubernetesGatewayService kubernetesGatewayService;


    @RequestMapping(value = "/gatewayapi", params = "Action=GetHTTPRoute", method = RequestMethod.GET)
    public String getHTTPRoute(@RequestParam(name = "GatewayName") String gateway) {
        Map<String, Object> result = new HashMap<>();
        List<HTTPRoute> kubernetesGateway = kubernetesGatewayService.getHTTPRoute(gateway);

        result.put(RESULT_LIST, kubernetesGateway);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(value = "/gatewayapi", params = "Action=GetKubernetesGateway", method = RequestMethod.GET)
    public String getKubernetesGateway(@RequestParam(name = "GatewayName", required = false) String gateway) {
        Map<String, Object> result = new HashMap<>();
        List<KubernetesGatewayDTO> kubernetesGateway = kubernetesGatewayService.getKubernetesGateway(gateway);
        result.put(RESULT_LIST, kubernetesGateway);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }
}
