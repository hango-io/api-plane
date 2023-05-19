package org.hango.cloud.web.controller;

import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class PortalGatewayController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(value = "/portal", params = "Action=PublishAPI", method = RequestMethod.POST)
    public String publishPortalAPI(@RequestBody @Valid PortalAPIDTO api) {
        gatewayService.updateAPI(api);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=DeleteAPI", method = RequestMethod.POST)
    public String deletePortalAPI(@RequestBody @Valid PortalAPIDeleteDTO api) {
        gatewayService.deleteAPI(api);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    /**
     * 新增或更新插件CRD配置
     *
     * @param plugin 本次最新的插件对象（CRD渲染需要的必要信息 + 插件配置集合）
     * @return 接口执行结果信息
     */
    @RequestMapping(value = "/portal", params = "Action=PublishPlugin", method = RequestMethod.POST)
    public String publishGatewayPlugin(@RequestBody @Valid GatewayPluginDTO plugin) {

        gatewayService.updateGatewayPlugin(plugin);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    /**
     * 删除路由插件CRD配置
     *
     * @param plugin 本次需要保留的插件对象（CRD渲染需要的必要信息 + 插件配置集合）
     * @return 接口执行结果信息
     */
    @RequestMapping(value = "/portal", params = "Action=DeletePlugin", method = RequestMethod.POST)
    public String deleteGatewayPlugin(@RequestBody @Valid GatewayPluginDTO plugin) {
        gatewayService.deleteGatewayPlugin(plugin);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=PublishService", method = RequestMethod.POST)
    public String publishPortalService(@RequestBody @Valid PortalServiceDTO service) {
        //新增参数校验逻辑
        ErrorCode errorCode = gatewayService.checkUpdateService(service);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        gatewayService.updateService(service);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=DeleteService", method = RequestMethod.POST)
    public String deletePortalService(@RequestBody @Valid PortalServiceDTO service) {
        gatewayService.deleteService(service);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=GetServiceHealthList", method = RequestMethod.GET)
    public String getServiceHealthList(@RequestParam(name = "Host", required = false) String host,
                                       @RequestParam(name = "Code") String serviceCode,
                                       @RequestParam(name = "Subsets") List<String> subsets,
                                       @RequestParam(name = "Gateway") String gateway) {

        String name = StringUtils.isEmpty(host) ? String.format("com.netease.%s", serviceCode.toLowerCase()) : host;

        Map<String, Object> result = new HashMap<>();
        List<ServiceHealth> gatewayList = gatewayService.getServiceHealthList(name, subsets, gateway);

        result.put(RESULT_LIST, gatewayList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(value = "/portal", params = "Action=UpdateIstioGateway", method = RequestMethod.POST)
    public String updatePortalGateway(@RequestBody @Valid PortalIstioGatewayDTO portalIstioGateway) {
        gatewayService.updateIstioGateway(portalIstioGateway);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=DeleteIstioGateway", method = RequestMethod.POST)
    public String deletePortalGateway(@RequestBody @Valid PortalIstioGatewayDTO portalIstioGateway) {
        gatewayService.deleteIstioGateway(portalIstioGateway);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=PublishSecret", method = RequestMethod.POST)
    public String updateSecret(@RequestBody @Valid PortalSecretDTO portalSecretDTO) {
        gatewayService.updateSecret(portalSecretDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=DeleteSecret", method = RequestMethod.POST)
    public String deleteSecret(@RequestBody @Valid PortalSecretDTO portalSecretDTO) {
        gatewayService.deleteSecret(portalSecretDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }
}