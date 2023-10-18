package org.hango.cloud.meta;

/**
 * @Author zhufengwei
 * @Date 2023/8/10
 */
public class BasePlugin {

    private String name;

    private String pluginType;

    private String language;

    private String pluginConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(String pluginConfig) {
        this.pluginConfig = pluginConfig;
    }
}
