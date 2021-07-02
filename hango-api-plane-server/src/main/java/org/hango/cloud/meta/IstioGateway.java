package org.hango.cloud.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
     * 配置是否记录上一代理的地址(默认false)
     */
    private String useRemoteAddress;


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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
