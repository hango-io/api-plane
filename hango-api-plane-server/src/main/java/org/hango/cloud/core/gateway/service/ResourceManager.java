package org.hango.cloud.core.gateway.service;

import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.Gateway;
import org.hango.cloud.meta.ServiceAndPort;
import org.hango.cloud.meta.ServiceHealth;

import java.util.List;

/**
 * 获取服务实例、网关实例等资源信息
 */
public interface ResourceManager {

    List<Endpoint> getEndpointList();

    List<Gateway> getGatewayList();

    List<String> getServiceList();

    List<ServiceAndPort> getServiceAndPortList();

    Integer getServicePort(List<Endpoint> endpoints, String targetHost);

    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);
}
