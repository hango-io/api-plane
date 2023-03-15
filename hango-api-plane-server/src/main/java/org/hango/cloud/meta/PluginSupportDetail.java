package org.hango.cloud.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/23
 */
public class PluginSupportDetail {
    /**
     * plugin schema 信息
     *
     * 该项可以为空，代表着
     * @see PluginSupportDetail#plugin 对应的插件配置不映射到插件schema中
     */
    private String schema;

    /**
     * 对应plugin manager 插件配置信息
     *
     * 该项可以为空，代表着
     * @see PluginSupportDetail#schema 对应的插件shema 无需对应开关开启即可使用
     */
    private String plugin;

    /**
     * 是否对外显示
     * 1. 如果对外显示，则该插件配置的开关由外部控制
     * 2. 如果不对外显示，该插件开关由 template/plugin/manager/plugin-manager-template.json 决定
     */
    private boolean display;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public boolean getDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }
}
