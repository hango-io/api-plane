package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @Author zhufengwei
 * @Date 2023/7/3
 */
public class ConfigMapDTO{

    /**
     * ConfigMap label
     */
    @JsonProperty(value = "Label")
    private Map<String, String> label;


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


    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
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
