package org.hango.cloud.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author zhangbj
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
     * 自定义Ip地址获取方式
     */
    private String customIpAddressHeader;

    /**
     * 配置记录XFF右起第几跳IP(默认为 0 )
     */
    private Integer xffNumTrustedHops;

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

    public String getCustomIpAddressHeader() {
        return customIpAddressHeader;
    }

    public void setCustomIpAddressHeader(String customIpAddressHeader) {
        this.customIpAddressHeader = customIpAddressHeader;
    }

    public Integer getXffNumTrustedHops() {
        return xffNumTrustedHops;
    }

    public void setXffNumTrustedHops(Integer xffNumTrustedHops) {
        this.xffNumTrustedHops = xffNumTrustedHops;
    }

    public String getUseRemoteAddress() {
        return useRemoteAddress;
    }

    public void setUseRemoteAddress(String useRemoteAddress) {
        this.useRemoteAddress = useRemoteAddress;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
