package org.hango.cloud.mcp.dao.meta;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/22
 **/
public class Resource {
    private String collection;
    private String name;
    private String label;
    private String config;

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}