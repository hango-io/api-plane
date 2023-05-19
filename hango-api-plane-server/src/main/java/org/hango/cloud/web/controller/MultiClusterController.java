package org.hango.cloud.web.controller;

import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.ResourceCheckResultDTO;
import org.hango.cloud.service.MultiClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:18
 **/
@RestController
@RequestMapping(value = "/util/resource", params = "Version=2022-09-15")
public class MultiClusterController  extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MultiClusterController.class);

    @Autowired
    private MultiClusterService multiClusterService;


    @RequestMapping(params = "Action=DataCheck", method = RequestMethod.POST)
    public String dataCheck(@RequestBody String resource) {
        logger.info("============================");
        logger.info("多集群校验 | 开始执行校验任务, 校验参数:{}", resource);
        ApiPlaneResult<Map<String, List<ResourceCheckResultDTO>>> result = multiClusterService.dataCheck(resource);
        if (result.isFailed()) {
            logger.error("校验异常，msg:{}", result.getErrorMsg());
            return apiReturn(result.getErrorCode(), result.getErrorMsg());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("Result", result.getData());
        return apiReturn(data);
    }
}
