package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;

public class PortalIstioGatewayDTO {

    /**
     * 网关名称
     */
    @JsonProperty(value = "Name")
    @NotEmpty
    private String name;

    /**
     * 网关集群信息
     */
    @JsonProperty(value = "GwCluster")
    @NotEmpty
    private String gwCluster;

    /**
     * 自定义Ip地址获取方式
     */
    @JsonProperty(value = "CustomIpAddressHeader")
    private String customIpAddressHeader;

    /**
     * 配置记录XFF右起第几跳IP(默认为1)
     */
    @JsonProperty(value = "XffNumTrustedHops")
    @Min(value = 1)
    private Integer xffNumTrustedHops = 1 ;

    /**
     * 配置是否记录上一代理的地址(默认false)
     */
    @JsonProperty(value = "UseRemoteAddress")
    private Boolean useRemoteAddress;

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

    public Boolean getUseRemoteAddress() {
        return useRemoteAddress;
    }

    public void setUseRemoteAddress(Boolean useRemoteAddress) {
        this.useRemoteAddress = useRemoteAddress;
    }

}
