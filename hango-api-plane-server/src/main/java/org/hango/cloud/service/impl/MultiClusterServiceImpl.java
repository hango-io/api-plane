package org.hango.cloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.cache.K8sResourceCache;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.service.impl.K8sConfigStore;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.k8s.K8sResourceApiEnum;
import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.ResourceCheckDTO;
import org.hango.cloud.meta.dto.ResourceCheckResultDTO;
import org.hango.cloud.meta.dto.ResourceDTO;
import org.hango.cloud.service.MultiClusterService;
import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:04
 **/
@Service
public class MultiClusterServiceImpl implements MultiClusterService {
    private static final Logger logger = LoggerFactory.getLogger(MultiClusterServiceImpl.class);

    @Autowired
    private K8sConfigStore k8sConfigStore;

    @Autowired
    private K8sResourceCache k8sResourceCache;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Autowired
    private GlobalConfig globalConfig;

    private final static String DATA_VERSION = "hango-data-version";

    @Override
    public ApiPlaneResult<Map<String, List<ResourceCheckResultDTO>>> dataCheck(String resourceStr) {
        ApiPlaneResult<ResourceCheckDTO> parseResult = parseResourceStr(resourceStr);
        if (parseResult.isFailed()){
            return ApiPlaneResult.ofFailed(parseResult.getErrorCode(), parseResult.getErrorMsg());
        }
        ResourceCheckDTO resourceCheckDTO = parseResult.getData();
        String gateway = resourceCheckDTO.getGateway();
        Map<String, List<ResourceDTO>> resourceMap = resourceCheckDTO.getResource();
        Map<String, List<ResourceCheckResultDTO>> result = new HashMap<>();
        for (Map.Entry<String, List<ResourceDTO>> entry : resourceMap.entrySet()) {
            List<ResourceCheckResultDTO> checkResultDTOS = checkResourceInfo(entry.getValue(), getResourceDTO(gateway, entry.getKey()));
            if (CollectionUtils.isNotEmpty(checkResultDTOS)){
                result.put(entry.getKey(), checkResultDTOS);
            }

        }
        return ApiPlaneResult.ofSuccess(result);
    }

    private ApiPlaneResult<ResourceCheckDTO> parseResourceStr(String resource){
        if (StringUtils.isEmpty(resource)){
            return ApiPlaneResult.ofFailed(ErrorCodeEnum.InvalidParameters, "校验数据为空");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode resources = objectMapper.readTree(resource);
            ResourceCheckDTO resourceCheckDTO = objectMapper.convertValue(resources, new TypeReference<ResourceCheckDTO>() {});
            return ApiPlaneResult.ofSuccess(resourceCheckDTO);
        } catch (JsonProcessingException e) {
            logger.error("解析请求体异常, response:{}", resource, e);
            return ApiPlaneResult.ofFailed(ErrorCodeEnum.InvalidParameters, "校验数据异常");
        }
    }

    private List<ResourceCheckResultDTO> checkResourceInfo(List<ResourceDTO> dbResource, List<ResourceDTO> crResource){
        if (dbResource == null) dbResource = new ArrayList<>();
        if (crResource == null) crResource = new ArrayList<>();
        Map<String, ResourceDTO> dbResourceMap = dbResource.stream().filter(o -> StringUtils.isNotBlank(o.getResourceName())).collect(
                Collectors.groupingBy(ResourceDTO::getResourceName, Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))));
        Map<String, ResourceDTO> crResourceMap = crResource.stream().filter(o -> StringUtils.isNotBlank(o.getResourceName())).collect(
                Collectors.groupingBy(ResourceDTO::getResourceName, Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))));

        Set<String> allResourceName = getAllResourceName(dbResource, crResource);
        List<ResourceCheckResultDTO> resourceCheckDTOS = new ArrayList<>();
        for (String resourceName : allResourceName) {
            ResourceDTO dbResourceDTO = dbResourceMap.get(resourceName);
            ResourceDTO crResourceDTO = crResourceMap.get(resourceName);
            String dbData = getResourceVersion(dbResourceDTO);
            String crData = getResourceVersion(crResourceDTO);
            if (!StringUtils.equals(dbData, crData)){
                Long resourceId = dbResourceDTO == null ? null : dbResourceDTO.getResourceId();
                resourceCheckDTOS.add(ResourceCheckResultDTO.of(resourceId, resourceName, dbData, crData));
            }
        }
        return resourceCheckDTOS;
    }

    private String getResourceVersion(ResourceDTO resourceDTO){
        if (resourceDTO == null){
            return null;
        }
        return StringUtils.isBlank(resourceDTO.getResourceVersion()) ? "0" : resourceDTO.getResourceVersion();
    }

    private Set<String> getAllResourceName(List<ResourceDTO> dbResource, List<ResourceDTO> drResource){
        Set<String> resourceName = new HashSet<>();
        List<String> dbResourceName = dbResource.stream().map(ResourceDTO::getResourceName).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        List<String> drResourceName = drResource.stream().map(ResourceDTO::getResourceName).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dbResourceName)){
            resourceName.addAll(dbResourceName);
        }
        if (CollectionUtils.isNotEmpty(drResourceName)){
            resourceName.addAll(drResourceName);
        }
        return resourceName;
    }

    public List<ResourceDTO> getResourceDTO(String gwName, String kind){
        return getCustomResource(gwName, kind).stream()
                .map(this::convert2ResourceDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<HasMetadata> getCustomResource(String gateway, String kind){
        //开启informer缓存，优先从cache中获取
        if (multiClusterK8sClient.watchResource()){
            List<HasMetadata> resource = k8sResourceCache.getResource(gateway, kind);
            if (K8sResourceApiEnum.EnvoyPlugin.name().equals(kind)){
                List<HasMetadata> smartLimit = k8sResourceCache.getResource(gateway, K8sResourceApiEnum.SmartLimiter.name());
                if (CollectionUtils.isNotEmpty(smartLimit)){
                    resource.addAll(smartLimit);
                }
            }
            return resource;
        }else {
            List<HasMetadata> resource = k8sConfigStore.get(kind, globalConfig.getResourceNamespace());
            if (K8sResourceApiEnum.EnvoyPlugin.name().equals(kind)){
                List<HasMetadata> smartLimit = k8sConfigStore.get(K8sResourceApiEnum.SmartLimiter.name(), globalConfig.getResourceNamespace());
                if (CollectionUtils.isNotEmpty(smartLimit)){
                    resource.addAll(smartLimit);
                }
            }
            return resource;
        }
    }


    private ResourceDTO convert2ResourceDTO(HasMetadata hasMetadata){
        ResourceDTO resourceDTO = new ResourceDTO();
        resourceDTO.setResourceName(hasMetadata.getMetadata().getName());
        Map<String, String> labels = hasMetadata.getMetadata().getLabels();
        if (labels == null){
            return null;
        }
        String versionStr = labels.get(DATA_VERSION);
        if (NumberUtils.isCreatable(versionStr)){
            resourceDTO.setResourceVersion(versionStr);
        }
        return resourceDTO;
    }

}