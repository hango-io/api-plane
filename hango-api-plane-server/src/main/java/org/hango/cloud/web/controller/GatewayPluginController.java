package org.hango.cloud.web.controller;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.meta.Plugin;
import org.hango.cloud.meta.PluginSupportDetail;
import org.hango.cloud.meta.dto.PluginOrderDTO;
import org.hango.cloud.meta.dto.PluginStatusDTO;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.service.PluginService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@SuppressWarnings("java:S1192")
@RestController
@RequestMapping(value = "/api/plugin", params = "Version=2019-07-25")
public class GatewayPluginController extends BaseController {
    @Autowired
    private PluginService pluginService;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private GatewayService gatewayService;


    /**
     * 查询插件schema
     */
    @RequestMapping(params = "Action=GetPluginDetail", method = RequestMethod.GET)
    public String getTemplate(@RequestParam("Name") String name) {

        Plugin plugin = pluginService.getPlugin(name);
        ErrorCode code = ApiPlaneErrorCode.Success;

        Map<String, Object> result = new HashMap<>();
        result.put("Schema", ResourceGenerator.newInstance(pluginService.getSchema(plugin.getSchema())).object(Object.class));
        result.put("Plugin", plugin);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    /**
     * 查询插件schema列表
     */
    @RequestMapping(params = "Action=GetPluginList", method = RequestMethod.GET)
    public String getPlugins(@RequestParam(name = "GatewayKind", required = false, defaultValue = "NetworkProxy") String gatewayKind) {
        List<PluginSupportDetail> pluginSupportDetails = pluginService.getPluginSupportConfig(gatewayKind);
        if ( CollectionUtils.isEmpty(pluginSupportDetails)) {
            return apiReturn(ImmutableMap.of("Plugins", Collections.emptyList()));
        }
        Map<String, Plugin> pluginMap = pluginService.getPlugins();
        if ( CollectionUtils.isEmpty(pluginMap)) {
            return apiReturn(ImmutableMap.of("Plugins", Collections.emptyList()));
        }

        List<Plugin> plugins = pluginSupportDetails.stream()
                .map(PluginSupportDetail::getSchema)
                .filter(pluginMap::containsKey)
                .filter(o -> !globalConfig.getIgnorePluginSet().contains(o))
                .map(pluginMap::get)
                .collect(Collectors.toList());
        return apiReturn(ImmutableMap.of("Plugins", plugins));
    }


    /**
     * 更新插件状态
     */
    @RequestMapping(params = "Action=UpdatePluginStatus", method = RequestMethod.POST)
    public String updatePluginStatus(@RequestBody PluginStatusDTO pluginStatusDTO) {
        gatewayService.updatePluginStatus(pluginStatusDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }


    /**
     * 发布plm
     */
    @RequestMapping(params = "Action=PublishPluginOrder", method = RequestMethod.POST)
    public String publishPluginOrder(@RequestBody @Valid PluginOrderDTO pluginOrderDTO) {
        boolean result = gatewayService.pluginOrderPortCheck(pluginOrderDTO);
        if (!result){
            return apiReturn(ApiPlaneErrorCode.PluginOrderPortError);
        }
        gatewayService.publishPluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    /**
     * 删除plm
     */
    @RequestMapping(params = "Action=DeletePluginOrder", method = RequestMethod.POST)
    public String deletePluginOrder(@RequestBody PluginOrderDTO pluginOrderDTO) {
        gatewayService.deletePluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    /**
     * 获取plm资源
     */
    @RequestMapping(params = "Action=GetPluginOrder", method = RequestMethod.GET)
    public String getPluginOrder(@RequestParam(name = "Name") String name) {
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, gatewayService.getPluginManager(name));
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    /**
     * 重新排序插件执行顺序
     */
    @RequestMapping(params = "Action=ResortPluginOrder", method = RequestMethod.GET)
    public String resortPluginOrder(@RequestParam("Names") List<String> names) {
        gatewayService.resortPluginOrder(names);
        return apiReturn(ApiPlaneErrorCode.Success);
    }
    /**
     * 重新全量加载全局插件
     */
    @RequestMapping(params = "Action=ReloadPluginOrder", method = RequestMethod.POST)
    public String reloadPluginOrder(@RequestBody @Valid PluginOrderDTO pluginOrderDTO) {
        gatewayService.reloadPluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }
}
