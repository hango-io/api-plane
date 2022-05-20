package org.hango.cloud.web.controller;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.meta.dto.PluginOrderDTO;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static org.hango.cloud.util.Const.VAILD_REGISTRY;

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
                                        @RequestParam(name = "Registry", required = false) String registry,
                                        @RequestParam Map<String, String> filters) {

        if (type != null) {
            if (!VAILD_REGISTRY.contains(type)) {
                return apiReturn(ApiPlaneErrorCode.ParameterError("Type"));
            }
        }
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null,
                ImmutableMap.of("ServiceList", gatewayService.getServiceAndPortList(name, type, registry, filters)));
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
    public String getDubboMeta(@RequestParam(name = "Igv") String igv) {

        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, gatewayService.getDubboMeta(igv));
        return apiReturn(result);

    }

}
