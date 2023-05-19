package org.hango.cloud.service;

import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.meta.dto.*;
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

    List<String> getServiceList();

    List<String> getRegistryList();

    List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId, Map<String, String> filters);


    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);

    /**
     * 获取Dubbo Meta元数据信息
     *
     * @param igv             接口+版本+分组 {interface:group:version}
     * @return
     */
    List<DubboMetaDto> getDubboMeta(String igv);


    void updateGrpcEnvoyFilter(GrpcEnvoyFilterDTO grpcEnvoyFilterDto);

    void deleteEnvoyFilter(EnvoyFilterDTO envoyFilterDTO);

    void updateIpSourceEnvoyFilter(IpSourceEnvoyFilterDTO ipSourceEnvoyFilterDto);

    void updateSecret(PortalSecretDTO portalSecretDTO);

    void deleteSecret(PortalSecretDTO portalSecretDTO);

    PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto);

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void publishPluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    void updateIstioGateway(PortalIstioGatewayDTO portalGateway);

    void deleteIstioGateway(PortalIstioGatewayDTO portalGateway);
}
