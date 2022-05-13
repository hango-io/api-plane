package org.hango.cloud.web.controller;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.meta.Gateway;
import org.hango.cloud.meta.dto.PluginOrderDTO;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class GatewayCommonController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(params = "Action=GetServiceList", method = RequestMethod.GET)
    public String getServiceList() {

        Map<String, Object> result = new HashMap<>();
        result.put(RESULT_LIST, gatewayService.getServiceList());
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=GetServiceAndPortList", method = RequestMethod.GET)
    public String getServiceAndPortList(@RequestParam(name = "Name", required = false) String name,
                                        @RequestParam(name = "Type", required = false) String type,
                                        @RequestParam(name = "Registry", required = false) String registry) {

        if (type != null) {
            if (!type.equals(Const.SERVICE_TYPE_CONSUL) && !type.equals(Const.SERVICE_TYPE_K8S) && !type.equals(Const.SERVICE_TYPE_DUBBO)) {
                return apiReturn(ApiPlaneErrorCode.ParameterError("Type"));
            }
        }
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null,
                ImmutableMap.of("ServiceList", gatewayService.getServiceAndPortList(name, type, registry)));
    }

    @RequestMapping(params = "Action=GetGatewayList", method = RequestMethod.GET)
    public String getGatewayList() {

        Map<String, Object> result = new HashMap<>();
        List<Gateway> gatewayList = gatewayService.getGatewayList();

        result.put(RESULT_LIST, gatewayList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=GetPluginOrder", method = RequestMethod.POST)
    public String getPluginOrder(@RequestBody PluginOrderDTO pluginOrderDTO) {
        Map<String, Object> result = new HashMap<>();

        result.put(RESULT, gatewayService.getPluginOrder(pluginOrderDTO));
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=PublishPluginOrder", method = RequestMethod.POST)
    public String publishPluginOrder(@RequestBody @Valid PluginOrderDTO pluginOrderDTO) {

        gatewayService.updatePluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = "Action=DeletePluginOrder", method = RequestMethod.POST)
    public String deletePluginOrder(@RequestBody @Valid PluginOrderDTO pluginOrderDTO) {

        gatewayService.deletePluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = "Action=GetDubboMeta", method = RequestMethod.GET)
    public String getDubboMeta(@RequestParam(name = "Igv", required = false) String igv,
                               @RequestParam(name = "Method", required = false) String method,
                               @RequestParam(name = "ApplicationName", required = false) String applicationName) {

        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, gatewayService.getDubboMeta(igv,applicationName,method));
        return apiReturn(result);

    }

}
