package org.hango.cloud.meta;

/**
 * @Author zhufengwei
 * @Date 2023/7/3
 */
public class PluginConfigInfo {

    /** 插件名称 */
    private String name;

    /** 插件显示名称 */
    private String displayName;

    /** 插件配置文件的路径 */
    private String schema;

    /** 插件的描述 */
    private String description;

    /** 插件作用范围 */
    private String pluginScope;

    /** 插件使用说明 */
    private String instructionForUse;

    /** 插件分类键 */
    private String categoryKey;

    /** 插件分类名称 */
    private String categoryName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getInstructionForUse() {
        return instructionForUse;
    }

    public void setInstructionForUse(String instructionForUse) {
        this.instructionForUse = instructionForUse;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
