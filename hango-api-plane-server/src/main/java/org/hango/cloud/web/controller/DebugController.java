package org.hango.cloud.web.controller;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.hango.cloud.cache.ResourceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/9/6 13:32
 **/
@RestController
@RequestMapping(value = "/debug")
public class DebugController extends BaseController{
    @Autowired
    ResourceCache resourceCache;

    @RequestMapping(params = "Action=GetResourceInfo", method = RequestMethod.GET)
    public String getResourceInfo(@RequestParam(name = "Kind") String kind, @RequestParam(name = "Gateway", required = false) String gateway) {
        List<HasMetadata> resource = resourceCache.getResource(gateway,kind);
        Map<String, Object> result = new HashMap<>();
        result.put("Resource", resource);
        return apiReturn(result);
    }

    @RequestMapping(params = "Action=GetResourceName", method = RequestMethod.GET)
    public String getResourceName(@RequestParam(name = "Kind") String kind) {
        List<String> resource = resourceCache.getResourceName(kind);
        Map<String, Object> result = new HashMap<>();
        result.put("Resource", resource);
        return apiReturn(result);
    }

}
