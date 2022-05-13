package org.hango.cloud.web.controller;

import org.hango.cloud.meta.dto.WhiteListV2AuthInfoDto;
import org.hango.cloud.meta.dto.WhiteListV2AuthStatusDto;
import org.hango.cloud.service.WhiteListV2Service;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @auther wengyanghui@corp.netease.com
 * @date 2020/04/10
 **/
@RestController
@RequestMapping(value = "/api/istio/rbac/v2", params = "Version=2020-04-10")
public class WhiteListV2Controller extends BaseController {

    @Autowired
    WhiteListV2Service whiteListV2Service;

    @RequestMapping(params = "Action=UpdateServiceAuthStatus", method = RequestMethod.POST)
    public String updateServiceAuthStatus(@RequestBody WhiteListV2AuthStatusDto authStatusDto) {
        whiteListV2Service.updateServiceAuth(authStatusDto.getService(), authStatusDto.getAuthOn(),
                authStatusDto.getDefaultPolicy(), authStatusDto.getAuthRules());
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=UpdateServiceAuthRules", method = RequestMethod.POST)
    public String updateServiceAuthRule(@RequestBody WhiteListV2AuthInfoDto authInfoDto) {
        whiteListV2Service.createOrUpdateAuthRule(authInfoDto.getService(), authInfoDto.getDefaultPolicy(),
                authInfoDto.getAuthRules());
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=DeleteServiceAuthRule", method = RequestMethod.POST)
    public String deleteServiceAuthRule(@RequestBody WhiteListV2AuthInfoDto authInfoDto) {
        if(authInfoDto.getRuleName() == null){
            return apiReturn(ApiPlaneErrorCode.MissingParamsError("rule name"));
        }
        whiteListV2Service.deleteAuthRule(authInfoDto.getService(), authInfoDto.getRuleName(),
                authInfoDto.getDefaultPolicy(), authInfoDto.getAuthRules());
        return apiReturn(SUCCESS, "Success", null, null);
    }

}
