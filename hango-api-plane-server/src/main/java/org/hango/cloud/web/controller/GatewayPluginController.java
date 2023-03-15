package org.hango.cloud.web.controller;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.meta.Plugin;
import org.hango.cloud.meta.PluginSupportConfig;
import org.hango.cloud.meta.PluginSupportDetail;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.service.PluginService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@RestController
@RequestMapping(value = "/api/plugin", params = "Version=2019-07-25")
public class GatewayPluginController extends BaseController {
    @Autowired
    private PluginService pluginService;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(params = "Action=GetPluginDetail", method = RequestMethod.GET)
    public String getTemplate(@RequestParam("Name") String name) {

        Plugin plugin = pluginService.getPlugin(name);
        ErrorCode code = ApiPlaneErrorCode.Success;

        Map<String, Object> result = new HashMap<>();
        result.put("Schema", ResourceGenerator.newInstance(pluginService.getSchema(plugin.getSchema())).object(Object.class));
        result.put("Plugin", plugin);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = "Action=GetPluginList", method = RequestMethod.GET)
    public String getPlugins(@RequestParam(name = "GatewayKind", required = false, defaultValue = "NetworkProxy") String gatewayKind) {
        Map<String, Plugin> pluginMap = pluginService.getPlugins();
        PluginSupportConfig pluginSupportConfig = gatewayService.getPluginSupportConfig(gatewayKind);
        if (pluginSupportConfig == null || CollectionUtils.isEmpty(pluginSupportConfig.getPlugins())) {
            return apiReturn(ImmutableMap.of("Plugins", Collections.emptyList()));
        }
        List<Plugin> plugins = Lists.newArrayList();
        List<String> pluginSupports = pluginSupportConfig.getPlugins().stream().map(PluginSupportDetail::getSchema).collect(Collectors.toList());
        Iterator<Map.Entry<String, Plugin>> iterator = pluginMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Plugin> next = iterator.next();
            if (pluginSupports.contains(next.getKey()) && !globalConfig.getIgnorePluginSet().contains(next.getKey())){
                plugins.add(next.getValue());
            }
        }
        return apiReturn(ImmutableMap.of("Plugins", plugins));
    }
}
