package org.hango.cloud.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.hango.cloud.meta.filter.ObjectMetaFilterMarker;
import org.hango.cloud.meta.filter.ObjectMetadataFilter;
import org.hango.cloud.meta.template.ServiceMeshTemplate;
import org.hango.cloud.service.TemplateService;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@RestController
@RequestMapping(value = "/api/istio", params = "Version=2018-05-31")
public class TemplateApiController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(TemplateApiController.class);

    @Autowired
    private TemplateService templateService;

    private ObjectMapper outMapper = new ObjectMapper().addMixIn(ObjectMeta.class, ObjectMetaFilterMarker.class)
            .setFilterProvider(
                    new SimpleFilterProvider().addFilter(
                            "objectMetadata", new ObjectMetadataFilter()));

    @RequestMapping(params = "Action=UpdateConfig", method = RequestMethod.POST)
    public String createConfig(@RequestBody @Valid ServiceMeshTemplate nsfTemplate) {

        templateService.updateConfig(nsfTemplate);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=DeleteConfig", method = RequestMethod.GET)
    public String deleteConfig(@RequestParam(value = "Kind", required = false) String kind,
                               @RequestParam("Namespace") String namespace,
                               @RequestParam("Name") String name,
                               @RequestParam(value = "Template", required = false) String template) {


        if (!StringUtils.isEmpty(kind)) {
            HasMetadata config = templateService.getConfig(name, namespace, kind);
            if (config == null) {
                return apiReturn(ApiPlaneErrorCode.resourceNotFound);
            }
            templateService.deleteConfig(name, namespace, kind);
        } else if (!StringUtils.isEmpty(template)) {
            List<HasMetadata> configList = templateService.getConfigListByTemplate(name, namespace, template);
            if (CollectionUtils.isEmpty(configList)) {
                return apiReturn(ApiPlaneErrorCode.resourceNotFound);
            }
            templateService.deleteConfigByTemplate(name, namespace, template);
        } else {
            return apiReturn(ApiPlaneErrorCode.MissingParamsError("kind or template"));
        }
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=GetConfigList", method = RequestMethod.GET)
    public String getConfigList(@RequestParam(value = "Kind", required = false) String kind,
                                @RequestParam("Namespace") String namespace,
                                @RequestParam(value = "Name") String name,
                                @RequestParam(value = "Template", required = false) String template) {

        Map<String, Object> result = new HashMap<>();
        if (!StringUtils.isEmpty(kind)) {
            HasMetadata resource = templateService.getConfig(name, namespace, kind);
            if (resource == null) {
                return apiReturn(ApiPlaneErrorCode.resourceNotFound);
            }
            result.put(RESULT, resource);
        } else if (!StringUtils.isEmpty(template)) {
            List<HasMetadata> resources = templateService.getConfigListByTemplate(name, namespace, template);
            if (CollectionUtils.isEmpty(resources)) {
                return apiReturn(ApiPlaneErrorCode.resourceNotFound);
            }
            result.put(RESULT_LIST, resources);
        } else {
            return apiReturn(ApiPlaneErrorCode.MissingParamsError("kind or template"));
        }
        return apiReturn(outMapper, SUCCESS, "Success", null, result);
    }
}
