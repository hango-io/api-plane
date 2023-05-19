package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;

/**
 * @Author zhufengwei
 * @Date 2023/5/10
 */
public class IpSourceEnvoyFilterDTO extends EnvoyFilterDTO{
    /**
     * 自定义Ip地址获取方式
     */
    @JsonProperty(value = "CustomIpAddressHeader")
    private String customIpAddressHeader;

    /**
     * 配置记录XFF右起第几跳IP(默认为0)
     */
    @JsonProperty(value = "XffNumTrustedHops")
    @Min(value = 0)
    private Integer xffNumTrustedHops = 0 ;

    /**
     * 配置是否记录上一代理的地址(默认true)
     */
    @JsonProperty(value = "UseRemoteAddress")
    private Boolean useRemoteAddress = true;

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
