package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
public class DubboInfoDto {

    /**
     * 方法
     */
    @JsonProperty(value = "Method")
    @NotEmpty
    private String method;

    /**
     * 关联ID
     */
    @JsonProperty(value = "ObjectId")
    @NotNull
    private Long objectId;

    /**
     * 关联类型，api/route 默认路由 route
     */
    @JsonProperty(value = "ObjectType")
    private String objectType;

    /**
     * 接口名
     */
    @JsonProperty(value = "InterfaceName")
    private String interfaceName;

    /**
     * 版本
     */
    @JsonProperty(value = "Version")
    private String version;

    /**
     * 是否开启自定义参数映射开关， 目前只支持开启
     */
    @JsonProperty(value = "CustomParamMapping")
    private boolean customParamMapping;

    /**
     * 分组
     */
    @JsonProperty(value = "Group")
    private String group;
    /**
     * <形参名>:<类型名>
     * Dubbo参数
     */
    @JsonProperty(value = "Params")
    private List<DubboParam> params = new ArrayList<>();


    /**
     * 参数来源，支持query和body两种参数来源配置
     */
    @JsonProperty(value = "ParamSource")
    @Pattern(regexp = "query|body")
    private String paramSource;

    /**
     * dubbo attachmentInfo
     */
    @JsonProperty(value = "Attachment")
    private List<DubboAttachmentDto> dubboAttachment;

    /**
     * 方法是否有效
     */
    @JsonProperty(value = "MethodWorks")
    private Boolean methodWorks;


    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<DubboParam> getParams() {
        return params;
    }

    public void setParams(List<DubboParam> params) {
        this.params = params;
    }

    public String getParamSource() {
        return paramSource;
    }

    public void setParamSource(String paramSource) {
        this.paramSource = paramSource;
    }

    public List<DubboAttachmentDto> getDubboAttachment() {
        return dubboAttachment;
    }

    public void setDubboAttachment(List<DubboAttachmentDto> dubboAttachment) {
        this.dubboAttachment = dubboAttachment;
    }

    public String getParamToStr() {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (DubboParam param : params) {
            builder.append(param.getKey()).append(":").append(param.getValue()).append(";");
        }
        return builder.substring(0, builder.lastIndexOf(";"));
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean getCustomParamMapping() {
        return customParamMapping;
    }

    public void setCustomParamMapping(boolean customParamMapping) {
        this.customParamMapping = customParamMapping;
    }

    public Boolean getMethodWorks() {
        return methodWorks;
    }

    public void setMethodWorks(Boolean methodWorks) {
        this.methodWorks = methodWorks;
    }

    public static class DubboMeta {

        /**
         * 方法
         */
        private String method;


        /**
         * 接口名
         */
        private String interfaceName;

        /**
         * 版本
         */
        private String version;

        /**
         * 分组
         */
        private String group;
        /**
         * <形参名>:<类型名>
         * Dubbo参数
         */
        private List<DubboParam> params;

        /**
         * 是否开启自定义参数映射开关， 默认false
         */
        private boolean customParamMapping;

        /**
         * 参数来源，支持：query和body三种参数来源配置
         */
        private String paramSource;

        /**
         * dubbo attachment信息
         */
        private List<DubboAttachmentDto> attachmentInfo;

        /**
         * dubbo 泛型信息
         */
        private String genericInfo;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public List<DubboParam> getParams() {
            return params;
        }

        public void setParams(List<DubboParam> params) {
            this.params = params;
        }

        public boolean getCustomParamMapping() {
            return customParamMapping;
        }

        public void setCustomParamMapping(boolean customParamMapping) {
            this.customParamMapping = customParamMapping;
        }

        public String getParamSource() {
            return paramSource;
        }

        public void setParamSource(String paramSource) {
            this.paramSource = paramSource;
        }

        public List<DubboAttachmentDto> getAttachmentInfo() {
            return attachmentInfo;
        }

        public void setAttachmentInfo(List<DubboAttachmentDto> attachmentInfo) {
            this.attachmentInfo = attachmentInfo;
        }

        public String getGenericInfo() {
            return genericInfo;
        }

        public void setGenericInfo(String genericInfo) {
            this.genericInfo = genericInfo;
        }
    }

    public static class DubboParam {
        /**
         * key
         */
        @JsonProperty(value = "Key")
        private String key;
        /**
         * value
         */
        @JsonProperty(value = "Value")
        private String value;

        @JsonProperty(value = "Required")
        private boolean required;

        @JsonProperty(value = "DefaultValue")
        private Object defaultValue;

        @JsonProperty(value = "GenericInfo")
        private String genericInfo;


        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public Object getDefaultJsonValue() throws JsonProcessingException {
            if (defaultValue == null) {
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(defaultValue);
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getGenericInfo() {
            return genericInfo;
        }

        public Map<String, String> getGenericMap() {
            if (StringUtils.isBlank(genericInfo)) {
                return null;
            }
            Map<String, String> map = Maps.newHashMap();
            String[] generics = StringUtils.split(genericInfo, ",");
            for (String genericStr : generics) {
                String[] generic = StringUtils.split(genericStr, ":");
                if (generic.length != 2) {
                    continue;
                }
                map.put(generic[0], generic[1]);
            }
            return map;
        }

        public void setGenericInfo(String genericInfo) {
            this.genericInfo = genericInfo;
        }
    }


    public static class DubboAttachmentDto {
        /**
         * attachment位置 Header/Cookie
         */
        @JsonProperty(value = "ParamPosition")
        private String paramPosition;
        /**
         * 客户端参数名称
         */
        @JsonProperty(value = "ClientParamName")
        private String clientParamName;

        /**
         * 服务端参数名称
         */
        @JsonProperty(value = "ServerParamName")
        private String serverParamName;

        /**
         * 备注信息
         */
        @JsonProperty(value = "Description")
        private String description;

        public String getParamPosition() {
            return paramPosition;
        }

        public void setParamPosition(String paramPosition) {
            this.paramPosition = paramPosition;
        }

        public String getClientParamName() {
            return clientParamName;
        }

        public void setClientParamName(String clientParamName) {
            this.clientParamName = clientParamName;
        }

        public String getServerParamName() {
            return serverParamName;
        }

        public void setServerParamName(String serverParamName) {
            this.serverParamName = serverParamName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDistinctName() {
            return paramPosition + clientParamName;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
