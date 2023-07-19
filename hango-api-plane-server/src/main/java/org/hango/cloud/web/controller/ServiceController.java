package org.hango.cloud.web.controller;

import com.google.common.collect.Maps;
import org.hango.cloud.meta.dto.KubernetesServiceDTO;
import org.hango.cloud.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/7/7
 */
@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class ServiceController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @GetMapping(params = "Action=GetServices")
    public String getServices(@RequestParam(name = "Namespace", required = false) String namespace,
                              @RequestParam(name = "Filters", required = false) Map<String, String> filters,
                              @RequestParam(name = "Domain", required = false) String domain) {
        List<KubernetesServiceDTO> kubernetesService = gatewayService.getKubernetesService(namespace, filters,domain);
        Map<String, Object> result = Maps.newHashMap();
        result.put(RESULT, kubernetesService);
        return apiReturn(result);
    }


}
