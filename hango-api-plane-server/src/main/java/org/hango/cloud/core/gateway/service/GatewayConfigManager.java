package org.hango.cloud.core.gateway.service;

import org.hango.cloud.core.ConfigManager;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.GrpcEnvoyFilterDto;

/**
 *  API配置客户端，用于发送配置
 */
public interface GatewayConfigManager extends ConfigManager {

    /**
     * 更新API
     * @param api
     */
    void updateConfig(API api);

    /**
     * 将插件配置转换为CRD资源，并调用API server接口更新
     *
     * @param plugin 网关插件对象
     */
    void updateConfig(GatewayPlugin plugin);

    /**
     * 更新服务
     * @param service
     */
    void updateConfig(Service service);

    /**
     * 删除API
     */
    void deleteConfig(API api);

    /**
     * 删除服务
     * @param service
     */
    void deleteConfig(Service service);

    /**
     * 获取插件优先级
     * @param pluginOrder
     * @return
     */
    HasMetadata getConfig(PluginOrder pluginOrder);

    /**
     * 更新插件优先级
     * @param pluginOrder
     */
    void updateConfig(PluginOrder pluginOrder);

    /**
     * 删除插件优先级
     * @param pluginOrder
     */
    void deleteConfig(PluginOrder pluginOrder);

    /**
     * 查询网关
     * @param istioGateway
     * @return
     */
    HasMetadata getConfig(IstioGateway istioGateway);

    /**
     * 更新网关
     * @param istioGateway
     * @return
     */
    void updateConfig(IstioGateway istioGateway);

    /**
     * 删除网关
     * @param istioGateway
     * @return
     */
    void deleteConfig(IstioGateway istioGateway);

    /**
     * 查询EnvoyFilter
     * @param envoyFilterOrder
     * @return
     */
    HasMetadata getConfig(EnvoyFilterOrder envoyFilterOrder);

    /**
     * 更新EnvoyFilter
     * @param envoyFilterOrder
     */
    void updateConfig(EnvoyFilterOrder envoyFilterOrder);
    /**
     * 删除EnvoyFilter
     * @param envoyFilterOrder
     */
    void deleteConfig(EnvoyFilterOrder envoyFilterOrder);

    String generateEnvoyConfigObjectPatch(GrpcEnvoyFilterDto grpcEnvoyFilterDto);


    /**
     * 更新证书
     */
    void updateConfig(Secret secret);

    /**
     * 删除证书
     */
    void deleteConfig(Secret secret);
}
