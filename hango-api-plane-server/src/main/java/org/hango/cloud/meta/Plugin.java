package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class Plugin {

    @JsonProperty("name")
    private String name;

    @JsonProperty("processor")
    private String processor;

    @JsonProperty("description")
    private String description;

    @JsonProperty("author")
    private String author;

    @JsonProperty("createTime")
    private String createTime;

    @JsonProperty("updateTime")
    private String updateTime;

    @JsonProperty("pluginScope")
    private String pluginScope;

    @JsonProperty("pluginPriority")
    private String pluginPriority;

    @JsonProperty("instructionForUse")
    private String instructionForUse;

    @JsonProperty("schema")
    private String schema;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("categoryKey")
    private String categoryKey;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("pluginGuidance")
    private String pluginGuidance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getPluginPriority() {
        return pluginPriority;
    }

    public void setPluginPriority(String pluginPriority) {
        this.pluginPriority = pluginPriority;
    }

    public String getInstructionForUse() {
        return instructionForUse;
    }

    public void setInstructionForUse(String instructionForUse) {
        this.instructionForUse = instructionForUse;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getPluginGuidance() {
        return pluginGuidance;
    }

    public void setPluginGuidance(String pluginGuidance) {
        this.pluginGuidance = pluginGuidance;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
