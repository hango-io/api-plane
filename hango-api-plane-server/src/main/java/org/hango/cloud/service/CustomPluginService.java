package org.hango.cloud.service;

import org.hango.cloud.meta.dto.CustomPluginPublishDTO;
import org.hango.cloud.meta.dto.CustomPluginCodeDTO;

/**
 * @Author zhufengwei
 * @Date 2023/9/26
 */
public interface CustomPluginService {

    /**
     * 添加自定义插件代码
     */
    boolean addPluginCodeFile(CustomPluginCodeDTO customPluginDTO);

    /**
     * 删除自定义插件代码
     */
    boolean deletePluginCodeFile(CustomPluginCodeDTO customPluginDTO);

    /**
     * 上线自定义插件
     */
    boolean onlineCustomPlugin(CustomPluginPublishDTO customPluginPublishDTO);

    /**
     * 下线自定义插件
     */
    boolean offlineCustomPlugin(CustomPluginPublishDTO customPluginPublishDTO);
}
