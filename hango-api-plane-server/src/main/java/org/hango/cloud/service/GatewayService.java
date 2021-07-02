package org.hango.cloud.service;

import org.hango.cloud.meta.*;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.util.errorcode.ErrorCode;

import java.util.List;

public interface GatewayService {

    void updateAPI(PortalAPIDTO portalAPI);

    void deleteAPI(PortalAPIDeleteDTO portalAPI);

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

    List<String> getServiceList();

    List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId);

    List<Gateway> getGatewayList();

    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);

    void updateIstioGateway(PortalIstioGatewayDTO portalGateway);

    PortalIstioGatewayDTO getIstioGateway(String clusterName);

    void updateGlobalPlugins(GlobalPluginDTO globalPluginsDTO);

    void deleteGlobalPlugins(GlobalPluginsDeleteDTO globalPluginsDeleteDTO);
}
