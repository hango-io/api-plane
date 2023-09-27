package org.hango.cloud.web.controller;

import org.hango.cloud.meta.dto.CustomPluginPublishDTO;
import org.hango.cloud.meta.dto.CustomPluginCodeDTO;
import org.hango.cloud.service.CustomPluginService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhufengwei
 * @Date 2023/9/26
 */
@RestController
@RequestMapping(value = "/api/customplugin")
public class CustomPluginController extends BaseController{

    @Autowired
    private CustomPluginService customPluginService;
    /**
     * 添加自定义插件代码
     */
    @RequestMapping(params = "Action=AddPluginCodeFile", method = RequestMethod.POST)
    public String addPluginCodeFile(@RequestBody @Valid CustomPluginCodeDTO customPluginDTO) {
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, customPluginService.addPluginCodeFile(customPluginDTO));
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    /**
     * 删除自定义插件代码
     */
    @RequestMapping(params = "Action=DeletePluginCodeFile",method = RequestMethod.POST)
    public String deletePluginCodeFile(@RequestBody CustomPluginCodeDTO customPluginDTO) {
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, customPluginService.deletePluginCodeFile(customPluginDTO));
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    /**
     * 上线自定义插件
     */
    @RequestMapping(params = "Action=OnlineCustomPlugin", method = RequestMethod.POST)
    public String onlineCustomPlugin(@RequestBody @Valid CustomPluginPublishDTO customPluginPublishDTO) {
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, customPluginService.onlineCustomPlugin(customPluginPublishDTO));
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    /**
     * 下线自定义插件
     */
    @RequestMapping(params = "Action=OfflineCustomPlugin", method = RequestMethod.POST)
    public String offlineCustomPlugin(@RequestBody @Valid CustomPluginPublishDTO customPluginPublishDTO) {
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, customPluginService.offlineCustomPlugin(customPluginPublishDTO));
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }
}
