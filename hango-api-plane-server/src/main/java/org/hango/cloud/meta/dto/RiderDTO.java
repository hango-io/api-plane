package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @Author zhufengwei
 * @Date 2023/9/22
 */
public class RiderDTO {
    @JsonProperty("pluginName")
    private String pluginName;


    @JsonProperty("imagePullSecretName")
    private String imagePullSecretName;

    @JsonProperty("url")
    @NotNull(message = "url")
    private String url;

    @JsonProperty("setting")
    private Object setting;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getImagePullSecretName() {
        return imagePullSecretName;
    }

    public void setImagePullSecretName(String imagePullSecretName) {
        this.imagePullSecretName = imagePullSecretName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getSetting() {
        return setting;
    }

    public void setSetting(Object setting) {
        this.setting = setting;
    }
}
