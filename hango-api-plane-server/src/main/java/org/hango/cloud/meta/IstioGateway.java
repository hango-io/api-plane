package org.hango.cloud.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 *
 * @version 1.0
 * @Type
 * @Desc 对应istio gateway资源
 * @date 2020/1/8
 */
public class IstioGateway {

    /**
     * 网关名称
     */
    private String name;

    /**
     * 网关集群信息
     */
    private String gwCluster;

    /**
     * server配置
     */
    private List<IstioGatewayServer> servers;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public List<IstioGatewayServer> getServers() {
        return servers;
    }

    public void setServers(List<IstioGatewayServer> servers) {
        this.servers = servers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
