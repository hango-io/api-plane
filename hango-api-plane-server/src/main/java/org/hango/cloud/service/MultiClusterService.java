package org.hango.cloud.service;

import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.ResourceCheckResultDTO;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 14:58
 **/
public interface MultiClusterService {
    ApiPlaneResult<Map<String, List<ResourceCheckResultDTO>>> dataCheck(String resource);
}
