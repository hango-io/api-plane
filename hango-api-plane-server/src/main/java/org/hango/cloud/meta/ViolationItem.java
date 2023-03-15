package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/9
 **/
public class ViolationItem {
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("namespace")
    private String namespace;
    @JsonProperty("name")
    private String name;
    @JsonProperty("message")
    private String message;
    @JsonProperty("validator")
    private String validator;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }
}
