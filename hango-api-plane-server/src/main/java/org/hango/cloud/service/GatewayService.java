package org.hango.cloud.service;

import org.hango.cloud.meta.PluginSupportConfig;
import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.meta.dto.DubboMetaDto;
import org.hango.cloud.meta.dto.EnvoyFilterDTO;
import org.hango.cloud.meta.dto.GatewayPluginDTO;
import org.hango.cloud.meta.dto.GrpcEnvoyFilterDto;
import org.hango.cloud.meta.dto.PluginOrderDTO;
import org.hango.cloud.meta.dto.PortalAPIDTO;
import org.hango.cloud.meta.dto.PortalAPIDeleteDTO;
import org.hango.cloud.meta.dto.PortalIstioGatewayDTO;
import org.hango.cloud.meta.dto.PortalSecretDTO;
import org.hango.cloud.meta.dto.PortalServiceDTO;
import org.hango.cloud.meta.dto.ServiceAndPortDTO;
import org.hango.cloud.util.errorcode.ErrorCode;

import java.util.List;
import java.util.Map;


public interface GatewayService {

    void updateAPI(PortalAPIDTO portalAPI);

    void deleteAPI(PortalAPIDeleteDTO portalAPI);

    void updateGatewayPlugin(GatewayPluginDTO plugin);

    void deleteGatewayPlugin(GatewayPluginDTO plugin);

    void updateService(PortalServiceDTO service);

    /**
     * 用于服务发布时参数校验
     *
     * @param service
     * @return
     */
    ErrorCode checkUpdateService(PortalServiceDTO service);

    void deleteService(PortalServiceDTO service);

    PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto);

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    void deleteEnvoyFilter(EnvoyFilterDTO envoyFilterOrder);

    List<String> getServiceList();

    List<String> getRegistryList();

    List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId, Map<String, String> filters);


    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);

    void updateIstioGateway(PortalIstioGatewayDTO portalGateway);

    void deleteIstioGateway(PortalIstioGatewayDTO portalGateway);

    PortalIstioGatewayDTO getIstioGateway(String clusterName);

    /**
     * 获取Dubbo Meta元数据信息
     *
     * @param igv             接口+版本+分组 {interface:group:version}
     * @return
     */
    List<DubboMetaDto> getDubboMeta(String igv);

    void updateEnvoyFilter(EnvoyFilterDTO grpcEnvoyFilterDTO);

    void updateGrpcEnvoyFilter(GrpcEnvoyFilterDto grpcEnvoyFilterDto);

    void deleteGrpcEnvoyFilter(GrpcEnvoyFilterDto grpcEnvoyFilterDto);

    void updateSecret(PortalSecretDTO portalSecretDTO);

    void deleteSecret(PortalSecretDTO portalSecretDTO);

    /**
     * 根据网关类型获取该类型所支持的插件列表
     *
     * @param gatewayKind
     * @return
     */
    PluginSupportConfig getPluginSupportConfig(String gatewayKind);

    /**
     * 根据网关类型获取PluginManagerTemplate
     * @param gatewayKind
     * @return
     */
    PluginOrderDTO getPluginOrderTemplate(String gatewayKind);
}
