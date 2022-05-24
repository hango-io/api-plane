package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hango.cloud.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class DubboMetaDto {

    /**
     * 主键ID
     */
    @NotNull
    @JsonProperty(value = "Id")
    private long id;


    /**
     * 应用名称
     */
    @NotBlank
    @JsonProperty(value = "ApplicationName")
    private String applicationName;


    /**
     * dubbo协议版本
     */
    @NotBlank
    @JsonProperty(value = "ProtocolVersion")
    private String protocolVersion;

    /**
     * dubbo协议版本
     */
    @NotNull
    @JsonProperty(value = "DubboAddress")
    private List<String> dubboAddress;


    /**
     * 接口名称
     */
    @NotBlank
    @JsonProperty(value = "InterfaceName")
    private String interfaceName;


    /**
     * 分组
     */
    
    @JsonProperty(value = "Group")
    private String group;


    /**
     * 版本
     */
    
    @JsonProperty(value = "Version")
    private String version;


    /**
     * 方法名称
     */
    @NotBlank
    @JsonProperty(value = "Method")
    private String method;


    /**
     * 参数列表
     */
    
    @JsonProperty(value = "RequestParams")
    private List<String> params;


    /**
     * 返回类型
     */
    
    @JsonProperty(value = "ResponseReturn")
    private String returns;


    /**
     * 创建时间
     */
    
    @JsonProperty(value = "CreateTime")
    private long createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }
    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    public String getIgv(){
        StringUtils.joinWith(":", interfaceName, group, version);
        return CommonUtil.removeEnd(":", interfaceName);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}