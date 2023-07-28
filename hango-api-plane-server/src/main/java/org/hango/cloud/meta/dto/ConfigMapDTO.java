package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2023/7/3
 */
public class ConfigMapDTO{
    /**
     * ConfigMap名称
     */
    @JsonProperty(value = "Name")
    private String name;


    /**
     * ConfigMap数据key
     */
    @JsonProperty(value = "ContentKey")
    private String contentKey;

    /**
     * ConfigMap数据
     */
    @JsonProperty(value = "ContentValue")
    private String contentValue;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getContentValue() {
        return contentValue;
    }

    public void setContentValue(String contentValue) {
        this.contentValue = contentValue;
    }
}
