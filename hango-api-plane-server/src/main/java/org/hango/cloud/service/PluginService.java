package org.hango.cloud.service;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.Plugin;
import org.hango.cloud.meta.PluginSupportDetail;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.meta.dto.PluginOrderDTO;

import java.util.List;
import java.util.Map;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface PluginService {

    /**
     * 查询插件
     */
    Plugin getPlugin(String name);

    /**
     * 查询全量插件
     */
    Map<String, Plugin> getPlugins();

    /**
     * 查询schema
     */
    String getSchema(String path);

    List<FragmentHolder> processPlugin(List<String> plugins, ServiceInfo serviceInfo);

    /**
     * 根据网关类型获取该类型所支持的插件列表
     */
    List<PluginSupportDetail> getPluginSupportConfig(String gatewayKind);


    /**
     * 根据网关类型获取该类型所支持的插件模板
     */
    PluginOrderDTO getPluginOrderTemplate(String gatewayKind);
}
