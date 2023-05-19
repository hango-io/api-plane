package org.hango.cloud.core.gateway.service;

import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.Gateway;
import org.hango.cloud.meta.ServiceAndPort;
import org.hango.cloud.meta.ServiceHealth;

import java.util.List;
import java.util.Map;

/**
 * 获取服务实例、网关实例等资源信息
 */
public interface ResourceManager {

    List<Endpoint> getEndpointList();


    List<String> getServiceList();

    List<ServiceAndPort> getServiceAndPortList(Map<String, String> filters);

    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);
}
