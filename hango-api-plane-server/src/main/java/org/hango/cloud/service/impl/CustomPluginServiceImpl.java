package org.hango.cloud.service.impl;

import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.meta.PluginOrder;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.meta.enums.PluginMappingEnum;
import org.hango.cloud.service.CustomPluginService;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.Trans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2023/9/26
 */
@Service
public class CustomPluginServiceImpl implements CustomPluginService {

    private static final Logger logger = LoggerFactory.getLogger(CustomPluginServiceImpl.class);

    @Autowired
    GatewayService gatewayService;

    @Autowired
    private GatewayConfigManager configManager;

    @Autowired
    private GlobalConfig globalConfig;

    @Override
    public boolean addPluginCodeFile(CustomPluginCodeDTO customPluginDTO) {
        //发布自定义代码
        boolean result = publishCustomCode(customPluginDTO);
        if (!result){
            logger.error("publish custom code failed, pluginName:{}", customPluginDTO.getPluginName());
            return false;
        }
        return true;
    }

    @Override
    public boolean deletePluginCodeFile(CustomPluginCodeDTO customPluginDTO) {
        //删除自定义代码
        boolean result = publishCustomCode(customPluginDTO);
        if (!result){
            logger.error("delete custom code failed, pluginName:{}", customPluginDTO.getPluginName());
            return false;
        }
        return true;
    }

    @Override
    public boolean onlineCustomPlugin(CustomPluginPublishDTO customPluginPublishDTO) {
        // 添加指定插件
        BiConsumer<CustomPluginPublishDTO, List<PluginOrderItemDTO>> addFunction = (dto, items) -> addCustomPlugin(items, dto);

        //更新插件配置
        return updateCustomPlugin(customPluginPublishDTO, addFunction);
    }


    @Override
    public boolean offlineCustomPlugin(CustomPluginPublishDTO customPluginPublishDTO) {
        //删除指定插件
        BiConsumer<CustomPluginPublishDTO, List<PluginOrderItemDTO>> deleteFunction = (deletePlugin, existedPlugins) -> {
            existedPlugins.removeIf(pluginOrderItemDTO -> pluginOrderItemDTO.getName().equals(deletePlugin.getPluginName()));
        };
        //更新插件配置
        return updateCustomPlugin(customPluginPublishDTO, deleteFunction);
    }

    public boolean updateCustomPlugin(CustomPluginPublishDTO customPluginPublishDTO, BiConsumer<CustomPluginPublishDTO, List<PluginOrderItemDTO>> updateFunction) {
        if (!StringUtils.hasText(customPluginPublishDTO.getPluginManagerName())
                || !StringUtils.hasText(customPluginPublishDTO.getPluginName())) {
            logger.error("pluginManagerName or pluginName is empty");
            return false;
        }

        // 查询plm资源
        PluginOrderDTO pluginManager = gatewayService.getPluginManager(customPluginPublishDTO.getPluginManagerName());
        if (CollectionUtils.isEmpty(pluginManager.getPlugins())) {
            logger.error("pluginManager:{} is empty", customPluginPublishDTO.getPluginManagerName());
            return false;
        }

        // 更新插件列表
        updateFunction.accept(customPluginPublishDTO, pluginManager.getPlugins());

        // 更新资源
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginManager);
        configManager.updateConfig(pluginOrder);
        return true;
    }


    /**
     * 发布自定义代码
     * @return 是否发布成功
     */
    private boolean publishCustomCode(CustomPluginCodeDTO customPluginDTO){
        ConfigMapDTO configMapDTO = new ConfigMapDTO();
        Map<String, String> label = new HashMap<>();
        label.put(Const.APP, Const.RIDER);
        label.put(Const.GW_CLUSTER, customPluginDTO.getGwCluster());
        configMapDTO.setContentKey(customPluginDTO.getPluginName());
        configMapDTO.setContentValue(customPluginDTO.getPluginContent());
        configMapDTO.setLabel(label);
        return gatewayService.publishConfigMap(configMapDTO);
    }

    private void addCustomPlugin(List<PluginOrderItemDTO> existedPlugins, CustomPluginPublishDTO addPlugin) {
        //设置默认值
        addDefaultInfo(addPlugin);
        //已存在同名插件，则进行更新
        PluginOrderItemDTO source = existedPlugins.stream().filter(item -> item.getName().equals(addPlugin.getPluginName())).findFirst().orElse(null);
        if (source != null){
            source.setWasm(addPlugin.getWasm());
            source.setRider(addPlugin.getLua());
            return;
        }

        //添加插件
        List<String> filterNames = existedPlugins.stream().map(PluginOrderItemDTO::getName).collect(Collectors.toList());
        int pluginIndex = PluginMappingEnum.getPluginIndex(filterNames, addPlugin.getPluginCategory());
        if (pluginIndex < 0 || pluginIndex > existedPlugins.size()) {
            logger.error("pluginIndex:{} is invalid", pluginIndex);
            throw new IllegalArgumentException("pluginIndex is invalid");
        }
        existedPlugins.add(pluginIndex, Trans.trans(addPlugin));
    }

    private void addDefaultInfo(CustomPluginPublishDTO customPluginPublishDTO){
        if (customPluginPublishDTO.getPort() == null) {
            customPluginPublishDTO.setPort(80);
        }
        if (customPluginPublishDTO.getWasm() != null){
            addDefaultSecret(customPluginPublishDTO.getWasm());
        }
        if (customPluginPublishDTO.getLua() != null){
            addDefaultSecret(customPluginPublishDTO.getLua());
        }
    }

    private void addDefaultSecret(RiderDTO riderDTO){
        if (riderDTO == null || !StringUtils.hasText(riderDTO.getUrl())){
            return;
        }
        if ( riderDTO.getUrl().startsWith("oci") && !StringUtils.hasText(riderDTO.getImagePullSecretName())){
            riderDTO.setImagePullSecretName(globalConfig.getDefaultSecretName());
        }
    }
}
