package org.hango.cloud.meta.dto;

/**
 * @Author zhufengwei
 * @Date 2023/9/22
 */
public class RiderDTO {
    private String pluginName;

    private String imagePullSecretName;

    private String url;

    private Object settings;

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

    public Object getSettings() {
        return settings;
    }

    public void setSettings(Object settings) {
        this.settings = settings;
    }
}
